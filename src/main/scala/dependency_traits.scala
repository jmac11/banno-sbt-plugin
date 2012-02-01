import sbt._
import reaktor.scct.ScctProject

trait BannoCommonDeps extends BasicScalaProject {
  lazy val jodaConvert = "org.joda" % "joda-convert" % "1.1"
  lazy val jodaTime = "joda-time" % "joda-time" % "2.0"
}

trait BannoAkkaProject extends BasicScalaProject {
  lazy val akkaVersion = buildScalaVersion match {
    case "2.8.1" => "1.0"
    case _ => "1.1.3"
  }

  def akkaModule(module: String) = "se.scalablesolutions.akka" % ("akka-" + module) % akkaVersion

  lazy val akkaActor = akkaModule("actor")
  lazy val akkaRemote = akkaModule("remote")

  // necessary since we depend on akka-kernel
  override def ivyXML =
  <dependencies>
    <exclude org="se.scalablesolutions.akka" module="akka-persistence"/>
  </dependencies>
}

trait HueDeps extends BasicScalaProject {
  lazy val htmlunit = "net.sourceforge.htmlunit" % "htmlunit" % "2.8"
  lazy val hue = "be.roam.hue" % "hue" % "1.1"
}

trait ScalaTestDeps extends BasicScalaProject {
  lazy val scalaTestVersion = "1.6.1"
  lazy val scalaTest = buildScalaVersion match {
    case "2.8.1" => "org.scalatest" %% "scalatest" % "1.5" % "test"
    case "2.9.0" => "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
    case "2.9.0-1" => "org.scalatest" % "scalatest_2.9.0" % scalaTestVersion % "test"
    case _ => "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  }
  lazy val awaitility = "com.jayway.awaitility" % "awaitility" % "1.3.1" % "test"
  lazy val scalaAwaitility = "com.jayway.awaitility" % "awaitility-scala" % "1.3.1" % "test"
}

trait SpecsTestDeps extends BasicScalaProject {
  lazy val specsVersion = "1.6.9"
  lazy val specs = "org.scala-tools.testing" %% "specs" % specsVersion % "test"
}

trait Specs2TestDeps extends BasicScalaProject {
  lazy val specs2Version = "1.5"
  lazy val specs2 = "org.specs2" %% "specs2" % specs2Version % "test"
  def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")
  override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)
}

trait LiftDeps extends BasicScalaProject {
  lazy val liftVersion = "2.2"
  lazy val liftWebKit = "net.liftweb" %% "lift-webkit" % liftVersion
  lazy val liftWizard = "net.liftweb" %% "lift-wizard" % liftVersion
  lazy val liftMapper = "net.liftweb" %% "lift-mapper" % liftVersion
  lazy val liftWidgets = "net.liftweb" %% "lift-widgets" % liftVersion
  lazy val liftRecord = "net.liftweb" %% "lift-record" % liftVersion
  lazy val liftSquerylRecord = "net.liftweb" %% "lift-squeryl-record" % liftVersion

  lazy val log4j = "log4j" % "log4j" % "1.2.16"
  lazy val slf4j = "org.slf4j" % "slf4j-log4j12" % "1.6.1"

  lazy val jettyServer = "org.mortbay.jetty" % "jetty" % "6.1.22" % "test"
  lazy val liftTestKit =  "net.liftweb" %% "lift-testkit" % liftVersion % "test"
}
