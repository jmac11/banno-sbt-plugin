package com.banno
import sbt._
import Keys._
import scala.xml._

object BannoIvy {
  val excludes =
    Seq(
      // all other crappy logging systems
      "commons-logging" -> "commons-logging",
      "log4j" -> "log4j",
      "org.slf4j" -> "slf4j-log4j12",
      "org.slf4j" -> "slf4j-simple",

      // why eclipse???
      "org.eclipse.jetty.orbit" -> "javax.servlet",

      // the beheamouth of unnecessary hbase deps
      "org.jruby" -> "jruby-complete",
      "com.sun.jersey" -> "jersey-core",
      "com.sun.jersey" -> "jersey-server",
      "com.sun.jersey" -> "jersey-json",
      "javax.ws.rs" -> "jsr311-api",
      "tomcat" -> "jasper-compiler",
      "tomcat" -> "jasper-runtime",
      "org.mortbay.jetty" -> "jetty",
      "org.mortbay.jetty" -> "jetty-util",
      "org.mortbay.jetty" -> "servlet-api",
      "org.mortbay.jetty" -> "servlet-api-2.5",
      "org.mortbay.jetty" -> "jsp-2.1",
      "org.mortbay.jetty" -> "jsp-api-2.1",
      "org.apache.hadoop" -> "avro",
      "org.apache.avro" -> "avro-ipc",
      "org.apache.thrift" -> "thrift"
    )

  def defaultExcludes(additionalExcludes: Pair[String,String]*) = {
    val allExcludes = excludes ++ additionalExcludes
    val allExcludesXml = allExcludes.map {
      case (org, module) =>
        <exclude org={org} module={module}/>
    }
    ivyXML ~= { deps => 
      deps match {
        case <dependencies>{children @ _*}</dependencies> =>
          <dependencies>{children ++ allExcludesXml}</dependencies>
        case NodeSeq.Empty =>
          <dependencies>{allExcludesXml}</dependencies>
      }
    }
  }
    
}
