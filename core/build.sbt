name := "snmp4s-core"

organization := "org.snmp4s"

version := "0.2.0"

scalaVersion := "2.11.8"

scalacOptions := Seq("-target:jvm-1.8", "-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions")

resolvers ++= Seq(
  "OO SNMP" at "https://oosnmp.net/dist/release/"
)

libraryDependencies ++= Seq(
  "org.snmp4j" % "snmp4j" % "2.5.2",
  "org.snmp4j" % "snmp4j-agent" % "2.5.3" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

osgiSettings

OsgiKeys.exportPackage := Seq(
  "org.snmp4s"
)

OsgiKeys.privatePackage := Seq()

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <scm>
    <url>git@github.com:barnesjd/snmp4s.git</url>
    <connection>scm:git:git@github.com:barnesjd/snmp4s.git</connection>
  </scm>
  <developers>
    <developer>
      <id>barnesjd</id>
      <name>Joe Barnes</name>
      <url>https://twitter.com/josefusbarnabas</url>
    </developer>
  </developers>)

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("http://snmp4s.org"))
