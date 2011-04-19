import sbt._
import sbt_akka_bivy._
import reaktor.scct.ScctProject

class DefaultBannoProject(info: ProjectInfo)
extends DefaultProject(info)
with BannoRepo
with BannoCommonDeps
with IdeaProject
with ScctProject
with CiTask
with SnapshotOrRelease
with UpdateMavenMetadataAfterPublish

class DefaultBannoAkkaProject(info: ProjectInfo)
extends DefaultBannoProject(info)
with BannoAkkaProject

class BannoFatAkkaKernelProject(info: ProjectInfo)
extends DefaultBannoAkkaProject(info)
with AkkaKernelDeployment {
  lazy val akkaKernel = akkaModule("kernel")
  override def akkaKernelBootClass = "akka.kernel.Main"
}

class BannoAkkaWebProject(info: ProjectInfo)
extends DefaultWebProject(info)
with BannoRepo
with BannoAkkaProject
with ScctProject
with CiTask
with SnapshotOrRelease
with UpdateMavenMetadataAfterPublish
