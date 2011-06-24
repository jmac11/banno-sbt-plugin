import sbt._
import Process._
import java.util.Enumeration
import java.io.File
import java.util.jar.JarFile

class Project(info: ProjectInfo) extends DefaultBannoProject(info) with FatJar {
  bannoDependency("com.banno", "test-dep")

  lazy val checkFile = task { (args) => task {
      val file = args(0)
      if (new java.io.File(file).exists) None else Some("File: " + file + " does not exist.")
    }
  }

  implicit def enum2Iterator[A](e : Enumeration[A]) = new Iterator[A] {
    def next = e.nextElement
    def hasNext = e.hasMoreElements
  }

  lazy val checkJarHas = task { (args) => task {
    val jarFile = args(0)
    val shouldContain = args(1)
    val f = new JarFile(new File(jarFile))
    if (f.getEntry(shouldContain) == null) {
      val entries: Iterator[_] = f.entries
      Some("Jar File %s expected to contain %s, but did not. Contains %s".format(jarFile, shouldContain, entries.toList))
    } else {
      None
    }
  }}

}
