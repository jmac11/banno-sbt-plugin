import sbt._

class Project(info: ProjectInfo) extends PluginProject(info) with test.ScalaScripted {
  val BannoExternalRepo   = "Banno External Repo" at "http://10.3.0.26:8081/nexus/content/groups/external/"
  val BannoSnapshotsRepo  = "Banno Snapshots Repo" at "http://10.3.0.26:8081/nexus/content/repositories/snapshots/"
  val BannoReleasesRepo   = "Banno Releases Repo" at "http://10.3.0.26:8081/nexus/content/repositories/releases/"

  override def managedStyle = ManagedStyle.Maven
  override def ivyRepositories = Resolver.defaultLocal(None) :: BannoExternalRepo :: BannoReleasesRepo :: BannoSnapshotsRepo ::  Nil

  Credentials(Path.userHome / ".ivy2" / ".banno_credentials", log)
  lazy val publishTo = BannoReleasesRepo

  override def compileAction = task {None}
  
  override def scriptedSbt = "0.7.5"
  override def scriptedBufferLog = false
  override def testAction = scripted

  override def releaseAction = (packageAction && incrementVersion) describedAs "Packages and increments the version"
  lazy val publishAndRelease = release dependsOn publish describedAs "Publishs artifacts and increments the version"

  val sbtAkkaBivy = "net.evilmonkeylabs" % "sbt-akka-bivy" % "0.2.0"
  val scctPlugin = "reaktor" % "sbt-scct-for-2.8" % "0.1-SNAPSHOT"
  val sbtIdea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.4.0"
}
