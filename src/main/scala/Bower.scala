package com.banno
import sbt._
import Keys._

object Bower {
  val bowerClientDirectory = SettingKey[File]("bower-client-directory")
  val bowerOutputDirectory = SettingKey[File]("bower-output-directory")
  val bowerExecutable = SettingKey[String]("bower-executable")
  val bower = TaskKey[Seq[File]]("bower")
  val npm = TaskKey[Unit]("npm")
  val bowerWatchSources = TaskKey[Seq[File]]("bower-watch-sources")

  val settings = Seq(
    bowerExecutable := "bower",
    bowerClientDirectory <<= baseDirectory apply (bd => bd / "src/main/client"),
    bowerOutputDirectory <<= (resourceManaged in Compile) apply (out => out / "public"),

    bowerWatchSources <<= bowerClientDirectory map (d => (d ***).get),
    watchSources <<= (watchSources, bowerWatchSources) map ( (ws, bws) => ws ++ bws),

    npm <<= (baseDirectory) map (Process("npm" :: "install" :: Nil, _) !),
    bowerExecutable <<= baseDirectory apply (bd => (bd / "node_modules/bower/bin/bower --allow-root").toString), // since bower is a npm dev dep

    bower <<= (bowerExecutable, baseDirectory, bowerOutputDirectory) map { (be, bd, out) =>
      val exitCode = (Process(be :: "install" :: Nil, bd, "OUTPUT_DIR" -> out.toString) !)
      if (exitCode != 0) sys.error(s"Bower nonzero exit code: ${exitCode}. Aborting!")
      (out ***).get
    },
    bower <<= bower.dependsOn(npm),
    resourceGenerators in Compile <+= bower.task
  )
}
