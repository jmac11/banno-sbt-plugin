package com.banno
import sbt._
import Keys._

object Grunt {
  val clientSrcDirectory = SettingKey[File]("client-src-directory")

  val npm = TaskKey[Unit]("npm")

  val bowerExecutable = SettingKey[File]("bower-executable")
  val bowerJsonFile = SettingKey[File]("bower-json-file")
  val bowerComponentsDirectory=  SettingKey[File]("bower-components-directory")
  val bower = TaskKey[Unit]("bower")

  val gruntWatchSources = TaskKey[Seq[File]]("grunt-watch-sources")
  val gruntExecutable = SettingKey[File]("grunt-executable")
  val gruntOutputDirectory = SettingKey[File]("output-directory")
  val gruntDefaultTask = SettingKey[String]("grunt-default-task")
  val grunt = TaskKey[Seq[File]]("grunt")

  lazy val settingsRunTime = generateSettings(false)
  lazy val settings = generateSettings(true)
  def generateSettings(generateOnCompile: Boolean) = Seq(
    clientSrcDirectory := baseDirectory.value / "src/main/client",

    // NPM
    npm :=  {
      val exitCode = (Process(
        "npm" :: "install" ::
        "--registry" :: "http://npm.banno-internal.com" ::
        "--color=false" :: "--unicode=false" :: "--spin=false" ::
        Nil,
        baseDirectory.value) !)
      if (exitCode != 0) sys.error("'npm install' failed!")
    },

    // bower
    bowerExecutable := baseDirectory.value / "node_modules/bower/bin/bower",
    bowerJsonFile := baseDirectory.value / "bower.json",
    bowerComponentsDirectory := baseDirectory.value / "bower_components",
    bower := {
      if (bowerJsonFile.value.exists) {
        val exitCode = (Process(bowerExecutable.value.absolutePath :: "install" :: "--allow-root" :: "--force-latest" :: Nil,
                                baseDirectory.value) !)
        if (exitCode != 0) sys.error(s"Bower nonzero exit code: ${exitCode}. Aborting!")
      }
    },
    bower <<= bower.dependsOn(npm),

    // Grunt
    gruntExecutable := baseDirectory.value / "node_modules/grunt-cli/bin/grunt",
    gruntOutputDirectory := (if (generateOnCompile)
                               (resourceManaged in Compile).value / "public"
                            else (resourceManaged in Runtime).value / "public"),
    gruntWatchSources := (clientSrcDirectory.value ***).get,
    watchSources := (watchSources.value ++ gruntWatchSources.value),
    gruntDefaultTask := "default",
    grunt := {
      val out = gruntOutputDirectory.value
      val exitCode = (Process(gruntExecutable.value.absolutePath :: gruntDefaultTask.value :: Nil, baseDirectory.value,
                              "OUTPUT_DIR" -> out.toString) !)
      if (exitCode != 0) sys.error(s"Grunt nonzero exit code: ${exitCode}. Aborting!")
      (out ***).get
    },
    grunt <<= grunt.dependsOn(bower),
    if (generateOnCompile)
      resourceGenerators in Compile <+= grunt
    else
      resourceGenerators in Runtime <+= grunt

  )
}
