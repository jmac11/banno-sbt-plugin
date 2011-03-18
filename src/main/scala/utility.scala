import sbt._
import reaktor.scct.ScctProject

trait BannoRepo extends BasicScalaProject {
  override def managedStyle = ManagedStyle.Maven

  import BannoNexusRepositories._
  override def ivyRepositories = Resolver.defaultLocal(None) ::
                                 ("Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository") ::
                                 BannoExternalRepo ::
                                 BannoReleasesRepo ::
                                 BannoSnapshotsRepo ::
                                 Nil

  Credentials(Path.userHome / ".ivy2" / ".banno_credentials", log)
  lazy val publishTo = BannoSnapshotsRepo
}

trait JRebelScan extends BasicWebScalaProject {
  // For use with JRebel - kills SBT's redeploy detection in favor of JRebel's
  override def scanDirectories = Nil
}

trait CiTask extends BasicScalaProject { scct: ScctProject =>
  lazy val ciActions = List(clean, cleanLib, update, test, scct.testCoverage, doc, publishLocal)
  lazy val ci = task {
    ciActions.foldLeft(None: Option[String]) { (result, task) => result orElse act(task.name) }
  } describedAs "Runs ci tasks"
}

