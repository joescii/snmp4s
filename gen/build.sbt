name := "snmp4s-gen"

organization := "org.snmp4s"

version := "0.2.0-SNAPSHOT"

scalaVersion := "2.10.6"

scalacOptions := Seq("-target:jvm-1.7", "-deprecation", "-unchecked")

resolvers += "Mibble" at "http://repo.opennms.org/maven2/"

libraryDependencies ++= Seq(
  "net.percederberg.mibble" % "mibble" % "2.9.3",
  "net.percederberg.mibble" % "mibble-mibs" % "2.9.3",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
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
