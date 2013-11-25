package com.banno
import sbt.IO
import java.io.{FileOutputStream, PrintWriter, File}
import sbtassembly.Plugin.MergeStrategy

object BannoAssembly {
  lazy val MergeStrategyConcatWithNewLine: MergeStrategy = new MergeStrategy {
    val name = "concatWithNewLine"

    def apply(tempDir: File, path: String, files: Seq[File]): Either[String, Seq[(File, String)]] = {
      val file = File.createTempFile("sbtMergeTarget", ".tmp", tempDir)
      val out = new FileOutputStream(file)
      val writer = new PrintWriter(out)
      try {
        files foreach { file =>
          IO.transfer(file, out)
          writer.print("\n")
          writer.flush()
        }
        Right(Seq(file -> path))
      } finally {
        out.close()
        writer.close()
      }
    }
  }
}
