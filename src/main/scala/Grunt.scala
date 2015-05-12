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

  val gulpExecutable = SettingKey[File]("gulp-executable")
  val gulpOutputDirectory = SettingKey[File]("output-directory")
  val gulpFile = SettingKey[File]("gulp-file")
  val gulpDefaultTask = SettingKey[String]("gulp-default-task")
  val gulp = TaskKey[Seq[File]]("gulp")

  val gruntWatchSources = TaskKey[Seq[File]]("grunt-watch-sources")
  val gruntExecutable = SettingKey[File]("grunt-executable")
  val gruntOutputDirectory = SettingKey[File]("output-directory")
  val gruntDefaultTask = SettingKey[String]("grunt-default-task")
  val grunt = TaskKey[Seq[File]]("grunt")

  val settings = Seq(
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

    // gulp
    gulpExecutable := baseDirectory.value / "node_modules/.bin/gulp",
    gulpOutputDirectory := (resourceManaged in Compile).value / "public",
    gulpFile := baseDirectory.value / "gulpfile.js",
    gulpDefaultTask := "default",
    gulp := {
      val out = gulpOutputDirectory.value
      if (gulpExecutable.value.exists && gulpFile.value.exists) {
        val exitCode = (Process(gulpExecutable.value.absolutePath :: gulpDefaultTask.value :: Nil,
                                baseDirectory.value, "OUTPUT_DIR" -> out.toString) !)
        if (exitCode != 0) sys.error(s"Gulp nonzero exit code: ${exitCode}. Aborting!")
      }
      (out ***).get
    },
    gulp <<= gulp.dependsOn(bower),
    resourceGenerators in Compile <+= gulp,

    // Grunt
    gruntExecutable := baseDirectory.value / "node_modules/grunt-cli/bin/grunt",
    gruntOutputDirectory := (resourceManaged in Compile).value / "public",
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
    resourceGenerators in Compile <+= grunt
  )
}
