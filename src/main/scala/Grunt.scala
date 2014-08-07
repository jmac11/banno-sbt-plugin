package com.banno
import sbt._
import Keys._

object Grunt {
  val npm = TaskKey[Unit]("npm")

  val bowerClientDirectory = SettingKey[File]("bower-client-directory")
  val bowerOutputDirectory = SettingKey[File]("bower-output-directory")
  val bowerExecutable = SettingKey[String]("bower-executable")
  val bower = TaskKey[Seq[File]]("bower")
  val bowerWatchSources = TaskKey[Seq[File]]("bower-watch-sources")

  val gruntClientDirectory = SettingKey[File]("grunt-client-directory")
  val gruntOutputDirectory = SettingKey[File]("grunt-output-directory")
  val gruntExecutable = SettingKey[String]("grunt-executable")
  val grunt = TaskKey[Seq[File]]("grunt")
  val gruntWatchSources = TaskKey[Seq[File]]("grunt-watch-sources")

  val settings = Seq(
    // NPM
    npm <<= (baseDirectory) map (Process("npm" :: "install" :: "--registry" :: "http://npm.banno-internal.com" :: Nil, _) !),

    // Bower
    bowerExecutable := "bower",
    bowerClientDirectory <<= baseDirectory apply (bd => bd / "src/main/client"),
    bowerOutputDirectory <<= (resourceManaged in Compile) apply (out => out / "public"),

    bowerWatchSources <<= bowerClientDirectory map (d => (d ***).get),
    watchSources <<= (watchSources, bowerWatchSources) map ( (ws, bws) => ws ++ bws),

    bowerExecutable <<= baseDirectory apply (bd => (bd / "node_modules/bower/bin/bower").toString),

    bower <<= (bowerExecutable, baseDirectory, bowerOutputDirectory) map { (be, bd, out) =>
      val exitCode = (Process(be :: "install" :: "--allow-root" :: "--force-latest" :: Nil, bd, "OUTPUT_DIR" -> out.toString) !)
      if (exitCode != 0) sys.error(s"Bower nonzero exit code: ${exitCode}. Aborting!")
      (out ***).get
    },
    bower <<= bower.dependsOn(npm),
    resourceGenerators in Compile <+= bower,

    // Grunt
    gruntExecutable := "grunt",
    gruntClientDirectory <<= baseDirectory apply (bd => bd / "src/main/client"),
    gruntOutputDirectory <<= (resourceManaged in Compile) apply (out => out / "public"),

    gruntWatchSources <<= gruntClientDirectory map (d => (d ***).get),
    watchSources <<= (watchSources, gruntWatchSources) map ( (ws, gws) => ws ++ gws),

    gruntExecutable <<= baseDirectory apply (bd => (bd / "node_modules/grunt-cli/bin/grunt").toString),

    grunt <<= (gruntExecutable, baseDirectory, gruntOutputDirectory) map { (ge, bd, out) =>
      val exitCode = (Process(ge, bd, "OUTPUT_DIR" -> out.toString) !)
      if (exitCode != 0) sys.error(s"Grunt nonzero exit code: ${exitCode}. Aborting!")
      (out ***).get
    },
    grunt <<= grunt.dependsOn(npm),
    grunt <<= grunt.dependsOn(bower),
    resourceGenerators in Compile <+= grunt
  )
}
