sbtPlugin := true

name := "snmp4s-sbt"

organization := "org.snmp4s"

version := "0.1.0"

scalaVersion := "2.9.2"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "org.snmp4s" %% "snmp4s-gen" % "0.1.0"
)

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

licenses := Seq("GNU GENERAL PUBLIC LICENSE Version 3" -> url("http://www.gnu.org/licenses/gpl.html"))

homepage := Some(url("http://snmp4s.org"))
