package com.banno
import sbt._
import Keys._
import sbtrelease._
import sbtrelease.ReleaseKeys._
import ReleaseStateTransformations._

object BannoRelease {

  val ignorableCodeChangePaths = SettingKey[Seq[String]]("ignorable-code-change-paths")
  val pushChanges = SettingKey[Boolean]("release-push-changes")

  val settings = Release.releaseSettings ++ Seq(
    ignorableCodeChangePaths := Seq(bannoDependenciesFileName, "version.sbt"),
    pushChanges := Option(System.getProperty("release.push")).map(_.toBoolean).getOrElse(true),

    commands += releaseIfChanged,

    tagName <<= (version in ThisBuild)(identity),
    releaseVersion <<= (organization, name, scalaVersion)(getLastVersionAndIncrement),
    nextVersion := removeMicroAndAddSnapshot,

    releaseProcess <<= thisProjectRef apply { ref =>
      Seq(
        initialGitChecks,
        inquireVersions,
        updateReleaseBannoDeps,
        setReleaseVersion,
        // checkSnapshotDependencies,
        runTest,
        commitReleaseBannoDepsVersions,
        commitReleaseVersion,
        tagRelease,
        releaseTask(publish in Global in ref),
        pushCurrentBranch,
        pushTag,
        setNextVersion,
        commitNextVersion,
        pushCurrentBranch
      )
    }

  )

  def removeMicroAndAddSnapshot(ver: String) = { Version(ver).map(_.copy(bugfix = None)).map(_.asSnapshot.string).getOrElse(versionFormatError) }

  def getLastVersionAndIncrement(org: String, name: String, scalaVers: String): (String => String) = { _ =>
    val tags = (Process("git" :: "tag" :: "-l" :: Nil) !!).split("\n").map(Version.apply).flatten
    val sortedTags = tags.sortWith { (a, b) =>
       a.major > b.major || a.minor.get > b.minor.get || a.bugfix.get >= b.bugfix.get
    }
    sortedTags.headOption.map(_.bumpBugfix.string).getOrElse(versionFormatError)
  }

  val releaseIfChanged: Command = Command.command("release-if-changed") {
    st: State =>
    if (bannoDependenciesHaveBeenUpdated(st) || codeChangedSinceLastRelease(st)) {
      val extracted = Project.extract(st)
      val process = extracted.get(releaseProcess)
      val startState = st.put(useDefaults, true)
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

  def latestReleasedVersionsForBannoDeps(st: State): Seq[Pair[ModuleID, String]] = {
    val extract = Project.extract(st)
    extract.get(bannoDependencies).map { dep =>
      val depArtifactId = dep.name + "_" + extract.get(scalaVersion)
      dep -> Nexus.latestReleasedVersionFor(dep.organization, depArtifactId).getOrElse(sys.error("No release found for %s".format(depArtifactId)))
    }
  }

  def updateReleaseBannoDeps(st: State): State = {
    st.log.info("Updating banno dependencies to latest releases")
    val withLatestRelease = latestReleasedVersionsForBannoDeps(st)

    // write to file
    val newSettingsContent =  withLatestRelease.map {
      case (dep, latest) =>
        st.log.info("updating \"%s\" to %s".format(dep, latest))
        "SettingKey[String](\"%s-released-version\") := \"%s\"\n\n".format(dep.name, latest)
    }

    if (!newSettingsContent.isEmpty) {
      IO.write(new File(bannoDependenciesFileName), newSettingsContent.mkString)
    }

    // reapply settings
    val newReleaseVersionSettings = withLatestRelease.map {
      case (dep, latest) =>
        val key = SettingKey[String]("%s-released-version".format(dep.name))
        key := latest
    }

    ReleaseStateTransformations.reapply(newReleaseVersionSettings, st)
  }

  def commitReleaseBannoDepsVersions(st: State): State = {
    val modified = Process("git" :: "status" :: "--porcelain" :: "--" :: bannoDependenciesFileName :: Nil) !! st.log
    if (!modified.isEmpty) {
      Git.add(bannoDependenciesFileName) !! st.log
      Git.commit("Updating banno dependencies to released versions") !! st.log
    }
    st
  }


  def codeChangedSinceLastRelease(st: State): Boolean = {
    val extract = Project.extract(st)
    val maybeLastRelease = Nexus.latestReleasedVersionFor(extract.get(organization),
                                                     extract.get(name) + "_" + extract.get(scalaVersion))
    maybeLastRelease.map { lastRelease =>
      val ignorablePaths = extract.get(ignorableCodeChangePaths)
      val diffRevisions = "%s..HEAD".format(lastRelease)
      val diff = (Process("git" :: "diff" :: diffRevisions :: "--name-only" :: Nil) !!).split("\n").toSet
      val realDiff = diff -- ignorablePaths
      !realDiff.isEmpty
    } getOrElse true
  }

  def pushCurrentBranch(st: State) = {
    val extract = Project.extract(st)
    if (extract.get(pushChanges)) {
      val currentBranch = Git.currentBranch
      val remoteBranch = "origin/%s".format(currentBranch)

      Process("git" :: "fetch" :: "origin" :: Nil) !! st.log
      if ((Process("git" :: "show-ref" :: remoteBranch :: Nil) ! st.log) == 0) {
        Process("git" :: "merge" :: remoteBranch :: Nil) !! st.log
      }

      Process("git" :: "push" :: "origin" :: "HEAD:%s".format(currentBranch) :: Nil) !! st.log
    }
    st
  }

  def pushTag(st: State) = {
    val extract = Project.extract(st)
    if (extract.get(pushChanges)) {
      val currentTagName = extract.get(tagName)
      Process("git" :: "push" :: "origin" :: currentTagName :: Nil) !! st.log
    }
    st
  }
}
