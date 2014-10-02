import com.banno._

name := "multi-project"

BannoSettings.settings

publishArtifact := false

lazy val subproj1 = bannoProject("multi-project-subproj1")

lazy val subproj2 = bannoProject("multi-project-subproj2").dependsOn(subproj1)
