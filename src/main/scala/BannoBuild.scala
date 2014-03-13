package com.banno
import sbt._
import Keys._

case class BannoBuild(id: String) extends Build {
  // this is weird since if we're the symlinked project we read from the root of the symlinkee project
  def findSymlinkedProjectFiles(cwd: File = file(".")): Seq[File] = {
    val currentSymlinkProjects = cwd.listFiles.filter(Symlink.isSymlinkDirectory)
    val allSymlinkProjects = for {
      dir <- currentSymlinkProjects
      symlinksUnderDir = findSymlinkedProjectFiles(dir)
    } yield (dir, symlinksUnderDir)

    val maybeUs = allSymlinkProjects.collectFirst {
      case (dir, symlinks) if dir.getName == id =>
        symlinks.map(f => file(f.getName))
    }
    maybeUs getOrElse currentSymlinkProjects
  }
  lazy val symlinkedProjects =
    findSymlinkedProjectFiles().map(sp => RootProject(sp): ClasspathDep[ProjectReference])

  lazy val proj = Project(id = id, base = file("."), dependencies = symlinkedProjects).settings(
    name := id
  ).settings(
    BannoSettings.settings: _*
  )
}

object Symlink {
  def isSymlinkDirectory(dir: File) = dir.exists && dir.isDirectory && isSymlink(dir)
  def isSymlink(file: File) = file.exists && {
    val canon = new File(file.getParentFile.getCanonicalFile, file.getName)
    canon.getCanonicalPath != canon.getAbsolutePath
  }
}

