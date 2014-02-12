package com.banno
import sbt._
import Keys._

object Specs2 {
  val version = SettingKey[String]("specs2-version")

  val settings: Seq[Setting[_]] = Seq(
    version <<= (scalaVersion) apply {
      case sv if sv.startsWith("2.9") => "1.12.4.1"
      case _ => "2.2.2"
    },
    libraryDependencies <+= (version) { v =>
      "org.specs2" %% "specs2" % v % "test"
    }
  )
}

object Scalacheck {
  val version = SettingKey[String]("scalacheck-version")

  val settings: Seq[Setting[_]] = Seq(
    version := "1.10.1",
    libraryDependencies <+= (version) { v =>
      "org.scalacheck" %% "scalacheck" % v % "test"
    }
  )
}


object ScalaTest {
  val version = SettingKey[String]("scalatest-version")

  val settings: Seq[Setting[_]] = Seq(
    version := "1.9.1",
    libraryDependencies <+= (version) { v =>
      "org.scalatest" %% "scalatest" % v % "test"
    }
  )

}

object HBaseTestingUtility {
  val hadoopVersion = SettingKey[String]("hadoop-version")
  val hbaseVersion = SettingKey[String]("hbase-version")

  // The "-tests" dependencies are really test classifier artifacts. However, ivy and sbt don't really get along with that.
  // i.e. "org.apache.hbase" % "hbase" % v % "test" classifier "tests",
  // So we had to manually upload them to nexus with a different artifact id i.e. hadoop-common became hadoop-common-tests

  val settings: Seq[Setting[_]] = Seq(
    hadoopVersion := "2.0.0-cdh4.3.0",
    hbaseVersion := "0.94.6-cdh4.3.0",
    libraryDependencies <++= (hadoopVersion) { v =>
      Seq("org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % v % "test",
          "org.apache.hadoop" % "hadoop-hdfs" % v % "test",
          "org.apache.hadoop" % "hadoop-common-tests" % v % "test",
          "org.apache.hadoop" % "hadoop-hdfs-tests" % v % "test",
          "org.apache.hadoop" % "hadoop-mapreduce-client-jobclient-tests" % v % "test")
    },
    libraryDependencies <++= (hbaseVersion) { v =>
      Seq("org.apache.hbase" % "hbase-tests" % v % "test",
          "log4j" % "log4j" % "1.2.17" % "test")
    }
  )
}
