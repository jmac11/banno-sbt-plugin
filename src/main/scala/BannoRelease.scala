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

object BannoRelease {

  val ignorableCodeChangePaths = SettingKey[Seq[String]]("ignorable-code-change-paths")
  val releaseFullClean = TaskKey[Unit]("release-full-clean")

  val settings = ReleasePlugin.releaseSettings ++ Seq(
    ignorableCodeChangePaths := Seq(bannoDependenciesFileName, "version.sbt"),

    releaseFullClean <<= target.map(IO.delete),
    aggregate in releaseFullClean := true,

    commands += releaseIfChanged,

    tagName <<= (version in ThisBuild) map identity,
    releaseVersion <<= (organization, name, scalaVersion)(getLastVersionAndIncrement),
    nextVersion := removeMinorAndAddSnapshot,

    releaseProcess := Seq[ReleaseStep](
      inquireVersions,
      updateReleaseBannoDeps,
      setReleaseVersion,

      // checkSnapshotDependencies,
      releaseTask(releaseFullClean),
      runTests,

      commitReleaseBannoDepsVersions,
      commitReleaseVersion,
      tagRelease,
      pushCurrentBranch,
      pushReleaseTag,
      buildAndPushDockerImage,
      publishArtifacts,

      setNextVersion,
      commitNextVersion,
      pushCurrentBranch
    )
  )

  def codeChangedSinceLastRelease(st: State): Boolean = {
    val extract = Project.extract(st)
    val artifactId = if (extract.get(crossPaths)) {
      extract.get(name) + "_" + CrossVersion.binaryScalaVersion(extract.get(scalaVersion))
    } else {
      extract.get(name)
    }
    val maybeLastRelease = Nexus.latestReleasedVersionFor(extract.get(organization), artifactId)
    maybeLastRelease.map { lastRelease =>
      val ignorablePaths = extract.get(ignorableCodeChangePaths)
      val diffRevisions = "%s..HEAD".format(lastRelease)
      val diff = (Git.cmd("diff", diffRevisions, "--name-only") !!).split("\n").toSet
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
      extract.get(key) != latestVersion
  }

  def removeMinorAndAddSnapshot(ver: String) = { Version(ver).map(_.copy(minor = None, bugfix = None)).map(_.asSnapshot.string).getOrElse(versionFormatError) }

  def getLastVersionAndIncrement(org: String, name: String, scalaVers: String): (String => String) = { _ =>
    val tags = (Git.cmd("tag", "-l") !!).split("\n").map(Version.apply).flatten
    VersionUtil.newestVersion(tags).map(_.bumpMinor.copy(bugfix = Some(0)).string).getOrElse("1.0.0")
  }

  def latestReleasedVersionsForBannoDeps(st: State): Seq[Pair[ModuleID, String]] = {
    val extract = Project.extract(st)
    extract.get(bannoDependencies).map { dep =>
      val depArtifactId =
        if (dep.crossVersion == CrossVersion.Disabled)
          dep.name
        else
          dep.name + "_" + CrossVersion.binaryScalaVersion(extract.get(scalaVersion))
      dep -> Nexus.latestReleasedVersionFor(dep.organization, depArtifactId).getOrElse(sys.error("No release found for %s".format(depArtifactId)))
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
                  val modified = Git.cmd("status", "--porcelain", "--", bannoDependenciesFileName) !! st.log
                  if (!modified.isEmpty) {
                    Git.add(bannoDependenciesFileName) !! st.log
                    Git.commit("Updating banno dependencies to released versions") !! st.log
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
              Tests.showResults(st.log, output, "No tests")
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

  object No {
    def unapply(str: Option[String]): Option[String] =
      if (str.exists(_.toLowerCase.startsWith("n"))) str else None
  }

  val pushCurrentBranch = ReleaseStep(
    action = (st: State) => {
      val defaultChoice = extractDefault(st, "y")
                                        (defaultChoice orElse SimpleReader.readLine("Push commits (y/n)? [y] : ")) match {
        case No() =>
          st.log.warn("Commits were not pushed. Please push them yourself.")
        case _ =>
          val currentBranch = Git.currentBranch
          val remoteName = "origin"
          val remoteBranch = "%s/%s".format(remoteName, currentBranch)

          Git.fetch(remoteName) !! st.log
          Git.cmd("merge", remoteBranch) !! st.log
          Git.cmd("push", "origin", "HEAD:%s".format(currentBranch)) !! st.log
      }
      st
    })

  val pushReleaseTag = ReleaseStep(
    action = (st: State) => {
      val defaultChoice = extractDefault(st, "y")
      (defaultChoice orElse SimpleReader.readLine("Push tag (y/n)? [y] : ")) match {
        case No() =>
          st.log.warn("Tag was not pushed. Please push them yourself.")
        case _ =>
          val extract = Project.extract(st)
          val (_, currentTagName) = extract.runTask(tagName, st)
          Git.cmd("push", "origin", currentTagName) !! st.log
      }
      st
    })

  val buildAndPushDockerImage = ReleaseStep(
    action = (st: State) => {
      val extract = Project.extract(st)
      val useDocker = extract.getOpt(Docker.packageUsingDocker)
      if (useDocker.getOrElse(false)) {
        extract.runTask(docker, st)
        extract.runTask(Docker.dockerPush, st)
        extract.runTask(Docker.dockerTagAsLatest, st)
        extract.runTask(Docker.dockerPushLatestTag, st)
      }
      st
    })
}
