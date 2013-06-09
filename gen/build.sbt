name := "snmp4s-gen"

organization := "org.snmp4s"

version := "0.2.0-SNAPSHOT"

scalaVersion := "2.9.2"

scalacOptions := Seq("-deprecation", "-unchecked")

resolvers += "Mibble" at "http://maven.cloudhopper.com/repos/third-party/"

libraryDependencies ++= Seq(
  "snmp" % "mibble-parser" % "2.9.2",
  "snmp" % "mibble-mibs" % "2.9.2",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
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
