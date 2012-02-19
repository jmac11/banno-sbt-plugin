import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val t8ExternalRepo = "Banno External Repo" at "http://10.3.0.26:8081/nexus/content/groups/external/"
  val licensePlugins = "com.banno" % "banno-sbt-plugin" % "latest.integration"
}
