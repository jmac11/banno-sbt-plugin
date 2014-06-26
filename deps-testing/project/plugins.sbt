// resolvers ++= Seq("Banno Snapshots Repo" at "http://nexus.banno.com/nexus/content/repositories/snapshots",
//                   "Banno Releases Repo" at "http://nexus.banno.com/nexus/content/repositories/releases",
//                   "Banno External Repo" at "http://nexus.banno.com/nexus/content/groups/external/")

// lazy val depsTesting = project in file("./deps-testing") dependsOn file("../plugin")

addSbtPlugin("com.banno" % "banno-sbt-plugin" % "1.4.3")
