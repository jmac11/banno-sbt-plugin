package com.banno
import sbt._
import Keys._

object SymlinkProjects {
  // this is weird since if we're the symlinked project we read from the root of the symlinkee project
  def findSymlinkedProjectFiles(currentRootProject: String, cwd: File = file(".")): Seq[File] = {
    val currentSymlinkProjects = cwd.listFiles.filter(Symlink.isSymlinkDirectory)
    val allSymlinkProjects = for {
      dir <- currentSymlinkProjects
      symlinksUnderDir = findSymlinkedProjectFiles(currentRootProject, dir)
    } yield (dir, symlinksUnderDir)

    val maybeUs = allSymlinkProjects.collectFirst {
      case (dir, symlinks) if dir.getName == currentRootProject =>
        symlinks.map(f => file(f.getName))
    }
    maybeUs getOrElse currentSymlinkProjects
  }

  def symlinkedProjects(currentRootProject: String) =
    findSymlinkedProjectFiles(currentRootProject).map(sp => RootProject(sp): ClasspathDep[ProjectReference])
}

object Symlink {
  def isSymlinkDirectory(dir: File) = dir.exists && dir.isDirectory && isSymlink(dir)
  def isSymlink(file: File) = file.exists && {
    val canon = new File(file.getParentFile.getCanonicalFile, file.getName)
    canon.getCanonicalPath != canon.getAbsolutePath
  }
}

