import sbt._
import de.element34.sbteclipsify._
import reaktor.scct.ScctProject

class DefaultBannoProject(info: ProjectInfo)
extends DefaultProject(info)
with BannoRepo
with BannoCommonDeps
with IdeaProject
with Eclipsify
with ScctProject
with CiTask
with SnapshotOrRelease
with UpdateMavenMetadataAfterPublish
with BannoReleaseProcess
with BannoCompilerOptions

class DefaultBannoAkkaProject(info: ProjectInfo)
extends DefaultBannoProject(info)
with BannoAkkaProject

class BannoFatAkkaKernelProject(info: ProjectInfo)
extends DefaultBannoAkkaProject(info)
with FatJar {
  lazy val akkaKernel = akkaModule("kernel")
  override def mainClass = Some("akka.kernel.Main")
}

class BannoFatStandaloneWebProject(info: ProjectInfo)
extends DefaultBannoProject(info)
with FatJar

class BannoAkkaWebProject(info: ProjectInfo)
extends DefaultWebProject(info)
with BannoRepo
with BannoAkkaProject
with IdeaProject
with Eclipsify
with ScctProject
with CiTask
with SnapshotOrRelease
with UpdateMavenMetadataAfterPublish
with BannoReleaseProcess

class BannoParentProject(info: ProjectInfo)
extends ParentProject(info)
with BannoMultiReleaseProcess
with BannoRepo
with UpdateMavenMetadataAfterPublish
