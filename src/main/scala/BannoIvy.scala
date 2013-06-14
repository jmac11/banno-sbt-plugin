package com.banno
import sbt._
import Keys._

object BannoIvy {
  val excludes =
    Seq(
      "org.slf4j" -> "slf4j-log4j12",
      "org.slf4j" -> "slf4j-simple",
      "org.eclipse.jetty.orbit" -> "javax.servlet",
      "org.mortbay.jetty" -> "servlet-api",
      "org.mortbay.jetty" -> "servlet-api-2.5",
      "org.jruby" -> "jruby-complete",
      "com.sun.jersey" -> "jersey-core",
      "javax.ws.rs" -> "jsr311-api",
      "tomcat" -> "jasper-compiler",
      "org.mortbay.jetty" -> "jsp-2.1",
      "org.mortbay.jetty" -> "jsp-api-2.1",
      "stax" -> "stax-api",
      "org.apache.avro" -> "avro-ipc",
      "org.apache.hadoop" -> "avro",
      "org.apache.thrift" -> "thrift",
      "commons-logging" -> "commons-logging",
      "log4j" -> "log4j"
    )

  def defaultExcludes(additionalExcludes: Pair[String,String]*) = {
    val allExcludes = excludes ++ additionalExcludes
    val allExcludesXml = allExcludes.map {
      case (org, module) =>
        <exclude org="{org}" module="{module}"/>
    }
    ivyXML ~= { deps => 
      deps match {
        case <dependencies>{children @ _*}</dependencies> =>
          <dependencies>{children ++ allExcludesXml}</dependencies>
      }
    }
  }
    
}
