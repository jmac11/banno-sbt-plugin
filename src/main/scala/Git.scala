import sbt._

object Git {
  def tag(tagName:String, msg: String, log: Logger): Option[String] = git("tag" :: "-a" :: "-m" :: msg :: tagName :: Nil, log)
  
  def commit(path: String, msg: String, log: Logger): Option[String] = git("add" :: path :: Nil, log) orElse git("commit" :: "-m" :: msg :: Nil, log)
  
  def checkout(ref: String, log: Logger): Option[String] = git("checkout" :: ref :: Nil, log)

  def pull(log: Logger): Option[String] = git("pull" :: Nil, log)
  
  def pushWithTags(ref: String, log: Logger): Option[String] = git("push" :: "origin" :: "--tags" :: ref :: Nil, log)
  
  def merge(ref: String, log: Logger): Option[String] = git("merge" :: ref :: Nil, log)

  def isDifference(diffRevisions: String, log: Logger): Boolean = {
    try {
      val diff = Process("git" :: "diff" :: diffRevisions :: Nil) !! (log)
      !diff.isEmpty
    } catch {
      case _ : Exception => true // hackety, hack, hack
    }
  }
  
  def hasRemote(remoteName: String, log: Logger): Boolean = {
    val diff = Process("git" :: "remote" :: Nil) !! (log)
    diff.contains(remoteName)
  }

  val RefspecRE = "ref: (.+)".r
  def currentHeadSHA(log: Logger): String = {
    val Right(head) = FileUtilities.readString(new java.io.File(".git/HEAD"), log)
    head.trim match {
      case RefspecRE(ref) => ref
      case sha => sha
    }
  }

  private def git(args: List[String], log: Logger): Option[String] = {
    log.info("running git " + args.mkString(" "))
    val exitCode = Process("git" :: args) ! (log)
    if (exitCode == 0) {
      None
    } else {
      Some("Command 'git %s' failed with exit code %s".format(args.mkString(" "), exitCode))
    }
  }
}
