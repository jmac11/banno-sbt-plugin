package com.banno
import sbt._
import Keys._

object ClientSideResources {
  val clientSideDir = settingKey[File]("Where all the client side files exist")
  val copyClientSide = taskKey[Seq[File]]("Copy the client side files under public/ resource managed.")

  lazy val settingsRunTime = generateSettings(false)
  lazy val settings = generateSettings(true)
  def generateSettings(generateOnCompile: Boolean) = Seq(
    clientSideDir := baseDirectory.value / "src/main/client",

    copyClientSide := {
      val rmPublic = if (generateOnCompile)
        (resourceManaged in Compile).value / "public"
      else
        (resourceManaged in Runtime).value / "public"
      IO.copyDirectory(clientSideDir.value, rmPublic, overwrite = true)
      (rmPublic ***).get
    },

    (if (generateOnCompile)
      resourceGenerators in Compile <+= copyClientSide
    else
      resourceGenerators in Runtime <+= copyClientSide),


    watchSources <<= (watchSources, clientSideDir) map ( (ws, cs) => ws ++ (cs ***).get)
  )
}
