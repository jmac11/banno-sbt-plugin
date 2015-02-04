package com.banno
import sbt._
import Keys._
import scala.xml._

object BannoIvy {
  val defaultExcludes =
    Seq(
      // all other crappy logging systems
      "commons-logging" -> "commons-logging",
      "log4j" -> "log4j",
      "org.slf4j" -> "slf4j-log4j12",
      "org.slf4j" -> "slf4j-simple",

      // kryo dupes
      "asm"-> "asm",
      "com.esotericsoftware.minlog" -> "minlog",

      // all other serlet-api's must go, use only "javax.servlet" % "javax.servlet-api" % "3.0.1"
      "org.eclipse.jetty.orbit" -> "javax.servlet",
      "javax.servlet" -> "servlet-api",
      "org.mortbay.jetty" -> "servlet-api",
      "org.mortbay.jetty" -> "servlet-api-2.5",

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
      "org.mortbay.jetty" -> "jsp-2.1",
      "org.mortbay.jetty" -> "jsp-api-2.1",
      "org.apache.hadoop" -> "avro",
      "org.apache.avro" -> "avro-ipc",
      "org.apache.thrift" -> "thrift"
    )


  def defaultOverrides(slf4jVersion: String) =
    Map[String, (String, String)] {
      slf4jVersion -> ( "org.slf4j" -> "slf4j-api")
    }

  val excludes = SettingKey[Seq[Pair[String, String]]]("banno-ivy-excludes")
  val overrides = SettingKey[Map[String, (String, String)]]("banno-ivy-overrides")

  val settings = Seq(
    excludes := defaultExcludes,
    excludesIvyXML,
    overrides := Map.empty,
    overrides <++= (BannoCommonDeps.slf4jVersion) { slf4jV => defaultOverrides(slf4jV) },
    overridesIvyXML
  )

  def addExclude(org: String, module: String) = addExcludes(org -> module)
  def addExcludes(modules: (String, String)*) = {
    val excludesXml: NodeSeq = modules.map {
      case (org, module) => <exclude org={org} module={module}/>
    }
    ivyXML := {
      ivyXML.value match {
        case ivyElem: Elem =>
          ivyElem.copy(child = (ivyElem.child ++ excludesXml))
        case NodeSeq.Empty =>
          <dependencies>{excludesXml}</dependencies>
      }
    }
  }

  def overrideVersion(version: String, modules: Pair[String,String]*) = {
    val allOverridesXml: NodeSeq = modules.map {
      case (org, module) => <override org={org} module={module} rev={version}/>
    }
    ivyXML := {
      ivyXML.value match {
        case ivyElem: Elem =>
          ivyElem.copy(child = (ivyElem.child ++ allOverridesXml))
        case NodeSeq.Empty =>
          <dependencies>{allOverridesXml}</dependencies>
      }
    }
 }

  def excludesIvyXML = {
    ivyXML := {
      val excludesXml = excludes.value.map {
        case (org, module) => <exclude org={org} module={module}/>
      }

      val logbackVersion: String = LogbackDeps.version.value
      val logbackOverrides = Seq(
        <override org="ch.qos.logback" module="logback-core" rev={logbackVersion}/>,
        <override org="ch.qos.logback" module="logback-classic" rev={logbackVersion}/>
      )

      ivyXML.value match {
        case ivyElem: Elem =>
          ivyElem.copy(child = (ivyElem.child ++ excludesXml ++ logbackOverrides))
        case NodeSeq.Empty =>
          <dependencies>{excludesXml ++ logbackOverrides}</dependencies>
      }
    }
  }

  def overridesIvyXML =
    ivyXML := {
      val overridesXml = overrides.value.map { case (version, (org, module)) =>
        <override org={org} module={module} rev={version}/>
      }
      ivyXML.value match {
        case ivyElem: Elem =>
          ivyElem.copy(child = (ivyElem.child ++ overridesXml))
        case NodeSeq.Empty =>
          <dependencies>{overridesXml}</dependencies>
      }
    }
}
