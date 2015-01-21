import com.banno._

name := "sbt-test-multi-project"

BannoSettings.settings

crossPaths := false

// publishArtifact := false // necessary so release can keep track of releases

addBannoDependency("banno-utils") // necessary so it picks up changes for the sub projects

BannoRelease.gitPushByDefault := false

lazy val subproj1 = bannoProject("sbt-test-multi-project-subproj1")
  .settings(publishArtifact := false)
  .settings(addBannoDependency("banno-utils"): _*)

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
