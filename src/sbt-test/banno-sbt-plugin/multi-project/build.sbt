import com.banno._

name := "sbt-test-multi-project"

BannoSettings.settings

crossPaths := false

// publishArtifact := false // necessary so release can keep track of releases

BannoRelease.gitPushByDefault := false

lazy val subproj1 = bannoProject("sbt-test-multi-project-subproj1")
  .settings(publishArtifact := false)
  .settings(addBannoDependencies("banno-utils", "kafka-utils"): _*)

lazy val subproj2 = bannoProject("sbt-test-multi-project-subproj2")
  .dependsOn(subproj1)
  .settings(publishArtifact := false)


lazy val deleteExistingReleases = taskKey[Unit]("Delete the existing multi-project releases")

deleteExistingReleases := {
  import dispatch.classic._
  import Http._
  val url = Nexus.releaseDirectoryPath("com.banno", "sbt-test-multi-project")
  try Http((Nexus.nexusAuthenticated(url) DELETE) >|)
  catch {
    case StatusCode(404, _) => // do nothing
  }
}
