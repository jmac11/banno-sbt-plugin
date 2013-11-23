package com.banno
import sbt.IO
import java.io.{FileOutputStream, PrintWriter, File}
import sbtassembly.Plugin.MergeStrategy

object BannoAssembly {
  lazy val MergeStrategyConcatWithNewLine: MergeStrategy = new MergeStrategy {
    val name = "concatWithNewLine"
    def apply(args: (File, String, Seq[File])): Either[String, Seq[(File, String)]] = {
      val file = File.createTempFile("sbtMergeTarget", ".tmp", args._1)
      val out = new FileOutputStream(file)
      val writer = new PrintWriter(out)
      try {
        args._3 foreach { file =>
          IO.transfer(file, out)
          writer.print("\n")
          writer.flush()
        }
        Right(Seq(file -> args._2))
      } finally {
        out.close()
        writer.close()
      }
    }
  }
}
