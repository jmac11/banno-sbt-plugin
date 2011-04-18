import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val bannoExternalRepo   = "Banno External Repo" at "http://10.3.0.26:8081/nexus/content/groups/external/"
  val scriptedDep = "org.scala-tools.sbt" % "scripted" % "0.7.4"
}
