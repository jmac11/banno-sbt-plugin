package com.banno
import sbt._
import Keys._
import sbtrelease._
import ReleasePlugin.ReleaseKeys._
import sbtrelease.Utilities._
import ReleaseStateTransformations._
import complete.DefaultParsers._
import BannoDependenciesVersionFile._
import sbtdocker._
import sbtdocker.Plugin.DockerKeys._
import scala.util.Try

object BannoRelease {

  val ignorableCodeChangePaths = SettingKey[Seq[String]]("ignorable-code-change-paths")
  val gitPushByDefault = SettingKey[Boolean]("release-default-git-push")
  val releaseFullClean = TaskKey[Unit]("release-full-clean")
  val lastReleaseGetter = SettingKey[(String, String) => Option[String]]("function to get last relase, given org and artifactId")

  val settings = ReleasePlugin.releaseSettings ++ Seq(
    ignorableCodeChangePaths := Seq(bannoDependenciesFileName, "version.sbt"),

    releaseFullClean <<= target.map(IO.delete),
    aggregate in releaseFullClean := true,

    gitPushByDefault := true,
    lastReleaseGetter := defaultLatestReleaseGetter,

    commands += releaseIfChanged,

    tagName <<= (version in ThisBuild) map identity,
    releaseVersion := getLastVersionAndIncrement,
    nextVersion := removeMinorAndAddSnapshot,

    releaseProcess := Seq[ReleaseStep](
      inquireVersions,
      updateReleaseBannoDeps,
      setReleaseVersion,

      // checkSnapshotDependencies,
      releaseTask(releaseFullClean),
      runTests,

      commitReleaseBannoDepsVersions,
      commitReleaseVersionWithGitStatus,
      tagRelease,
      pushReleaseTag,
      buildAndPushDockerImage,
      publishArtifacts,

      setNextVersion,
      commitNextVersion,
      pushCurrentBranch
    )
  )

  private lazy val git: Git =
    Git.mkVcs(file("."))

  def defaultLatestReleaseGetter(org: String, artifactId: String): Option[String] =
    Nexus.latestReleasedVersionFor(org, artifactId)

  def codeChangedSinceLastRelease(st: State): Boolean = {
    val extract = Project.extract(st)
    val artifactId = if (extract.get(crossPaths)) {
      extract.get(name) + "_" + CrossVersion.binaryScalaVersion(extract.get(scalaVersion))
    } else {
      extract.get(name)
    }
    val org = extract.get(organization)
    val lrGetter = extract.get(lastReleaseGetter)
    val maybeLastRelease = lrGetter(org, artifactId)
    maybeLastRelease.map { lastRelease =>
      val ignorablePaths = extract.get(ignorableCodeChangePaths)
      val diffRevisions = "%s..HEAD".format(lastRelease)
      val diff = (git.cmd("diff", diffRevisions, "--name-only") !!).split("\n").toSet
      val realDiff = diff -- ignorablePaths
      !realDiff.isEmpty
    } getOrElse true
  }

  val releaseIfChanged: Command = Command("release-if-changed")(_ => (Space ~> "skip-tests").*) { (st, args) =>
    if (bannoDependenciesHaveBeenUpdated(st) || codeChangedSinceLastRelease(st)) {
      val extracted = Project.extract(st)
      val releaseParts = extracted.get(releaseProcess)
      val startState =
        st.put(useDefaults, true)
          .put(skipTests, args.contains("skip-tests"))

      val initialChecks = releaseParts.map(_.check)
      val process = releaseParts.map(_.action)

      initialChecks.foreach(_(startState))
      Function.chain(process)(startState)
    } else {
      st.log.info("No changes so no release")
      st
    }
  }

  def bannoDependenciesHaveBeenUpdated(st: State): Boolean = latestReleasedVersionsForBannoDeps(st).exists {
    case (dep, latestVersion) =>
      val extract = Project.extract(st)
      val key = SettingKey[String]("%s-released-version".format(dep.name))
      extract.get(key in Global) != latestVersion
  }

  def removeMinorAndAddSnapshot(ver: String) = { Version(ver).map(_.copy(minor = None, bugfix = None)).map(_.asSnapshot.string).getOrElse(versionFormatError) }

  def tags = (git.cmd("tag", "-l") !!).split("\n")

  def taggedVersions = tags.map(Version.apply).flatten

  def getLastVersionAndIncrement: (String => String) = { _ =>
    VersionUtil.newestVersion(taggedVersions).map(_.bumpMinor.copy(bugfix = Some(0)).string).getOrElse("1.0.0")
  }

  def latestReleasedVersionsForBannoDeps(st: State): Seq[Pair[ModuleID, String]] = {
    val extract = Project.extract(st)
    val lrGetter = extract.get(lastReleaseGetter)
    extract.get(bannoDependencies).map { dep =>
      val depArtifactId =
        if (dep.crossVersion == CrossVersion.Disabled)
          dep.name
        else
          dep.name + "_" + CrossVersion.binaryScalaVersion(extract.get(scalaVersion))
      dep -> lrGetter(dep.organization, depArtifactId).getOrElse(sys.error("No release found for %s".format(depArtifactId)))
    }
  }

  val updateReleaseBannoDeps =
    ReleaseStep(action = (st: State) => {
                  st.log.info("Updating banno dependencies to latest releases")
                  val withLatestRelease = latestReleasedVersionsForBannoDeps(st)

                  writeBannoDependenciesVersionsToFile(st.log, withLatestRelease)
                  // reapply settings
                  val newReleaseVersionSettings = withLatestRelease.map {
                    case (dep, latest) =>
                      val key = SettingKey[String]("%s-released-version".format(dep.name))
                      key in Global := latest
                  }

                  ReleaseStateTransformations.reapply(newReleaseVersionSettings, st)
                })


  val commitReleaseBannoDepsVersions =
    ReleaseStep(action = (st: State) => {
                  val modified = git.cmd("status", "--porcelain", "--", bannoDependenciesFileName) !! st.log
                  if (!modified.isEmpty) {
                    git.add(bannoDependenciesFileName) !! st.log
                    git.commit("Updating banno dependencies to released versions") !! st.log
                  }
                  st
                })

  val runTests = ReleaseStep(
    action = (st: State) => {
      if (!st.get(skipTests).getOrElse(false)) {
        val extracted = Project.extract(st)
        val ref = extracted.get(thisProjectRef)
        val (_, results) = SbtCompat.runTaskAggregated(executeTests in Test in ref, st)
        results match {
          case Value(outputs) =>
            outputs.foreach { case Aggregation.KeyValue(k, output) =>
              st.log.info(s"For ${projectNameOfScopedKey(k)}")
              TestResultLogger.Default.run(st.log, output, "test")
            }
            val failed = outputs.filterNot {
              case Aggregation.KeyValue(k, output) => output.overall == TestResult.Passed
            }
            if (failed.nonEmpty) {
              val failedProjects = failed.map(kv => projectNameOfScopedKey(kv.key)).mkString(",")
              sys.error(s"Failed tests for ${failedProjects}! Aborting release.")
            } else {
              st.log.info("PASSED ALL TESTS")
            }
            st
          case _ => st
        }
      } else st
    },
    enableCrossBuild = true
  )

  private[this] def projectNameOfScopedKey(key: ScopedKey[_]) =
    key.scope.project.asInstanceOf[Select[ProjectRef]].s.project


  lazy val initialVcsChecksWithGitStatus = { st: State =>
    val diff = (git.cmd("diff") !!).trim
    val status = (git.status !!).trim
    if (status.nonEmpty) {
      st.log.info("Git Status:")
      st.log.info(status)
      st.log.info("Git Diff:")
      st.log.info(diff)
      sys.error("Aborting release. Working directory is dirty.")
    }

    st.log.info("Starting release process off commit: " + git.currentHash)
    st
  }

  val commitReleaseVersionWithGitStatus = ReleaseStep(commitReleaseVersion.action,
                                                      initialVcsChecksWithGitStatus)

  object No {
    def unapply(str: Option[String]): Option[String] =
      if (str.exists(_.toLowerCase.startsWith("n"))) str else None
  }

  val pushCurrentBranch = ReleaseStep(
    action = (st: State) => {
      val defaultChoice = extractDefault(st, ynGitPushByDefault(st))
      (defaultChoice orElse SimpleReader.readLine("Push commits (y/n)? [y] : ")) match {
        case No() =>
          st.log.warn("Commits were not pushed. Please push them yourself.")
        case _ =>
          val currentBranch = git.currentBranch
          val remoteName = "origin"
          val remoteBranch = "%s/%s".format(remoteName, currentBranch)

          git.fetch(remoteName) !! st.log
          git.cmd("merge", remoteBranch) !! st.log
          git.cmd("push", "origin", "HEAD:%s".format(currentBranch)) !! st.log
      }
      st
    })

  def ynGitPushByDefault(st: State): String = {
    val extracted = Project.extract(st)
    if (extracted.get(gitPushByDefault))
      "y"
    else
      "n"
  }

  val pushReleaseTag = ReleaseStep(
    action = (st: State) => {
      val defaultChoice = extractDefault(st, ynGitPushByDefault(st))
      (defaultChoice orElse SimpleReader.readLine("Push tag (y/n)? [y] : ")) match {
        case No() =>
          st.log.warn("Tag was not pushed. Please push them yourself.")
        case _ =>
          val extract = Project.extract(st)
          val (_, currentTagName) = extract.runTask(tagName, st)
          git.cmd("push", "origin", currentTagName) !! st.log
      }
      st
    })

  val buildAndPushDockerImage = ReleaseStep(
    action = (st: State) => {
      val extracted = Project.extract(st)
      val ref = extracted.get(thisProjectRef)
      SbtCompat.runTaskAggregated(Docker.dockerPullLatest in ref, st)
      SbtCompat.runTaskAggregated(docker in ref, st)
      SbtCompat.runTaskAggregated(Docker.dockerPush in ref, st)
      SbtCompat.runTaskAggregated(Docker.dockerPushLatestTag in ref, st)
      st
    })
}
