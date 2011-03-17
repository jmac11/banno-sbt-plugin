import sbt._

class Project(info: ProjectInfo) extends PluginProject(info) {
  val BannoExternalRepo   = "Banno External Repo" at "http://10.3.0.26:8081/nexus/content/repositories/external/"
  val BannoSnapshotsRepo  = "Banno Snapshots Repo" at "http://10.3.0.26:8081/nexus/content/repositories/snapshots/"
  val BannoReleasesRepo   = "Banno Releases Repo" at "http://10.3.0.26:8081/nexus/content/repositories/releases/"

  override def managedStyle = ManagedStyle.Maven
  override def ivyRepositories = Resolver.defaultLocal(None) :: BannoExternalRepo :: BannoReleasesRepo :: BannoSnapshotsRepo ::  Nil

  Credentials(Path.userHome / ".ivy2" / ".banno_credentials", log)
  lazy val publishTo = BannoReleasesRepo

  override def compileAction = task {None}

  val akkaPlugin = "se.scalablesolutions.akka" % "akka-sbt-plugin" % "1.0"
  val sbtAkkaBivy = "net.evilmonkeylabs" % "sbt-akka-bivy" % "0.2.0"
  val scctPlugin = "reaktor" % "sbt-scct-for-2.8" % "0.1-SNAPSHOT"
  val sbtIdea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.2.0"
}
