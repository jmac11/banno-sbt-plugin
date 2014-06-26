package com.banno
import sbt._
import Keys._

object ClientSideResources {
  val clientSideDir = settingKey[File]("Where all the client side files exist")
  val copyClientSide = taskKey[Seq[File]]("Copy the client side files under public/ resource managed.")

  val settings = Seq(
    clientSideDir := baseDirectory.value / "src/main/client",

    copyClientSide := {
      val rmPublic = (resourceManaged in Compile).value / "public"
      IO.copyDirectory(clientSideDir.value, rmPublic, overwrite = true)
      (rmPublic ***).get
    },

    resourceGenerators in Compile <+= copyClientSide,

    watchSources <<= (watchSources, clientSideDir) map ( (ws, cs) => ws ++ (cs ***).get)
  )
}
