name := "banno-sbt-plugin"

organization := "com.banno"

sbtPlugin := true

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.4")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "0.4.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

libraryDependencies += "net.databinder" %% "dispatch-http" % "0.8.8"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps")

releaseSettings

publishTo := {
  if (version.value.endsWith("SNAPSHOT"))
    Some("Banno Snapshots Repo" at "http://nexus.banno.com/nexus/content/repositories/snapshots")
  else
    Some("Banno Releases Repo" at "http://nexus.banno.com/nexus/content/repositories/releases")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".banno_credentials")

publishMavenStyle := true
