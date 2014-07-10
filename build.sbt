name := "banno-sbt-plugin"

version := "1.4.4"

organization := "com.banno"

sbtPlugin := true

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.10.1")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.3")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.1")

libraryDependencies += "net.databinder" %% "dispatch-http" % "0.8.8"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps")

publishTo := {
  if (version.value.endsWith("SNAPSHOT"))
    Some("Banno Snapshots Repo" at "http://nexus.banno.com/nexus/content/repositories/snapshots")
  else
    Some("Banno Releases Repo" at "http://nexus.banno.com/nexus/content/repositories/releases")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".banno_credentials")

publishMavenStyle := true
