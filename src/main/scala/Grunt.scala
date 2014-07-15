package com.banno
import sbt._
import Keys._

object Grunt {
  val gruntClientDirectory = SettingKey[File]("grunt-client-directory")
  val gruntOutputDirectory = SettingKey[File]("grunt-output-directory")
  val gruntExecutable = SettingKey[String]("grunt-executable")
  val grunt = TaskKey[Seq[File]]("grunt")
  val npm = TaskKey[Unit]("npm")
  val gruntWatchSources = TaskKey[Seq[File]]("grunt-watch-sources")

  val settings = Seq(
    gruntExecutable := "grunt",
    gruntClientDirectory <<= baseDirectory apply (bd => bd / "src/main/client"),
    gruntOutputDirectory <<= (resourceManaged in Compile) apply (out => out / "public"),

    gruntWatchSources <<= gruntClientDirectory map (d => (d ***).get),
    watchSources <<= (watchSources, gruntWatchSources) map ( (ws, gws) => ws ++ gws),

    npm <<= (baseDirectory) map (Process("npm" :: "install" :: Nil, _) !),
    gruntExecutable <<= baseDirectory apply (bd => (bd / "node_modules/grunt-cli/bin/grunt").toString), // since grunt-cli is a npm dev dpe

    grunt <<= (gruntExecutable, baseDirectory, gruntOutputDirectory) map { (ge, bd, out) =>
      val exitCode = (Process(ge, bd, "OUTPUT_DIR" -> out.toString) !)
      if (exitCode != 0) sys.error(s"Grunt nonzero exit code: ${exitCode}. Aborting!")
      (out ***).get
    },
    grunt <<= grunt.dependsOn(npm),
    resourceGenerators in Compile <+= grunt.task
  )
}
