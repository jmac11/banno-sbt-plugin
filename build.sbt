name := "banno-sbt-plugin"

organization := "com.banno"

sbtPlugin := true

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.2")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.1.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.github.tkawachi" % "sbt-doctest" % "0.3.4")

libraryDependencies += "net.databinder" %% "dispatch-http" % "0.8.10"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.5",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.5"
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps")

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoPackage := "com.banno"

buildInfoKeys := Seq[BuildInfoKey](version, sbtBinaryVersion, scalaBinaryVersion)

ScriptedPlugin.scriptedSettings

scriptedBufferLog := false

scriptedLaunchOpts := Seq(
  "-XX:MaxPermSize=128m",
  "-Xmx512m",
  "-Dplugin.version=" + version.value
)

test := scripted.toTask("").value

val clearLocal = Def.task {
  println("Removing old published/cached banno-sbt-plugin's")
  IO.delete(Path.userHome / ".ivy2" / "cache" / "scala_2.10" / "sbt_0.13" / "com.banno" / "banno-sbt-plugin")
  IO.delete(Path.userHome / ".ivy2" / "local" / "com.banno" / "banno-sbt-plugin")
}

publishLocal := {
  val cl = clearLocal.value
  publishLocal.value
}

releaseSettings

publishTo := {
  if (version.value.endsWith("SNAPSHOT"))
    Some("Banno Snapshots Repo" at "http://nexus.banno.com/nexus/content/repositories/snapshots")
  else
    Some("Banno Releases Repo" at "http://nexus.banno.com/nexus/content/repositories/releases")
}

publishArtifact in (Compile, packageSrc) := false

publishArtifact in (Compile, packageDoc) := false

credentials += Credentials(Path.userHome / ".ivy2" / ".banno_credentials")

publishMavenStyle := true
