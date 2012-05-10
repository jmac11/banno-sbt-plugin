package com.banno
import sbt._
import Keys._
import sbtrelease._
import sbtrelease.ReleaseKeys._

object BannoRelease {
  val settings = Release.releaseSettings ++ Seq(
    tagName <<= (version in ThisBuild)(identity),
    releaseVersion <<= (organization, name, scalaVersion) { (g, a, s) => { _ =>
      val latestReleasedVersion = Nexus.latestReleasedVersionFor(g, a + "_" + s)
      latestReleasedVersion.flatMap(v => Version(v).map(_.bumpBugfix.string)).getOrElse(versionFormatError)
    }},
    nextVersion := { ver => Version(ver).map(_.copy(bugfix = None)).map(_.asSnapshot.string).getOrElse(versionFormatError) },
    releaseProcess <<= thisProjectRef apply { ref =>
      import ReleaseStateTransformations._
      Seq[ReleasePart](
        initialGitChecks,
        // checkSnapshotDependencies,
        inquireVersions,
        updateReleaseBannoDeps,
        commitReleaseBannoDepsVersions,
        setReleaseVersion,
        runTest,
        commitReleaseVersion,
        tagRelease,
        releaseTask(publish in Global in ref),
        setNextVersion,
        commitNextVersion,
        push
      )
    }
  )

  val updateReleaseBannoDeps: ReleasePart = { st =>
    val extract = Project.extract(st)
    st.log.info("Updating banno dependencies to latest releases")
    val bannoDeps = extract.get(bannoDependencies)
    val withLatestRelease = bannoDeps.map { dep =>
      dep -> Nexus.latestReleasedVersionFor(dep.organization, dep.name).getOrElse("1.0.001")
    }

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

  val commitReleaseBannoDepsVersions: ReleasePart = { st =>
    Git.add(bannoDependenciesFileName) !! st.log
    Git.commit("Updating banno dependencies to released versions") !! st.log
    st
  }

  val push: ReleasePart = { st =>
    Git.pushTags !! st.log
    Git.pushCurrentBranch !! st.log
    st
  }
}
