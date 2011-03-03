import sbt._
import sbt_akka_bivy._
import reaktor.scct.ScctProject

trait BannoCommonDeps extends BasicScalaProject {
  lazy val jodaTime = "joda-time" % "joda-time" % "1.6"
  lazy val scalajCollection = "org.scalaj" % "scalaj-collection_2.8.0" % "1.0"
}

class DefaultBannoProject(info: ProjectInfo)
extends DefaultProject(info)
with BannoCommonDeps
with IdeaProject
with ScctProject {

  lazy val t8Repo     = "internal" at "http://10.3.0.26:8080/archiva/repository/internal/"

  override def managedStyle = ManagedStyle.Maven
  lazy val publishTo = t8Repo
}

trait BannoAkkaProject extends AkkaProject {
  lazy val akkaRemote = akkaModule("remote")

  // necessary since we depend on akka-kernel
  // which has it's own dependencies (eg. terrastore-javaclient, aws-java-sdk, et al) that also depend on another version of jackson
  // this causes ivy to do something stupid and choose the wrong transitive dependency
  // this breaks akka-remote actors
  override def ivyXML =
  <dependencies>
    <override org="org.codehaus.jackson" module="jackson-mapper-asl" rev="1.4.3"/>
    <override org="org.codehaus.jackson" module="jackson-core-asl" rev="1.4.3"/>
  </dependencies>
}

class DefaultBannoAkkaProject(info: ProjectInfo)
extends DefaultBannoProject(info)
with BannoAkkaProject

class BannoFatAkkaKernelProject(info: ProjectInfo)
extends DefaultBannoAkkaProject(info)
with AkkaKernelDeployment {
  lazy val akkaKernel = akkaModule("kernel")

  override def akkaKernelBootClass = "akka.kernel.Main"
}

trait ScalaTestDeps extends BasicScalaProject {
  lazy val scalaTest = "org.scalatest" % "scalatest" % "1.3" % "test"
  lazy val awaitility = "com.jayway.awaitility" % "awaitility" % "1.3.1" % "test"
  lazy val scalaAwaitility = "com.jayway.awaitility" % "awaitility-scala" % "1.3.1" % "test"
}

trait SpecsTestDeps extends BasicScalaProject {
  lazy val specs = "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5" % "test"
}
