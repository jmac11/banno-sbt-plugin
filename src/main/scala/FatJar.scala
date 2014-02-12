package com.banno
import sbt._
import Keys._
import sbtassembly._
import sbtassembly.Plugin
import Plugin.AssemblyKeys._

object FatJar {
  val settings: Seq[Setting[_]] = Plugin.assemblySettings ++ Seq(
    mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
      {
        case "reference.conf" => BannoAssembly.MergeStrategyConcatWithNewLine
        case "application.conf" => BannoAssembly.MergeStrategyConcatWithNewLine
        case x => old(x)
      }
    },
    packagedArtifacts <<= (packagedArtifacts, assembly, name) map ( (arts, file, n) => arts.updated(Artifact(n, "assembly"), file) ),
    artifacts <+= (name) { n => Artifact(n, "assembly") },
    test in assembly := { }
  )
}
