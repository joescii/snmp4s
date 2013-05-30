name := "snmp4s-core"

organization := "org.snmp4s"

version := "0.1.0"

scalaVersion := "2.10.1"

scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions")

resolvers ++= Seq(
  "OO SNMP" at "https://oosnmp.net/dist/release/"
)

libraryDependencies ++= Seq(
  "org.snmp4j" % "snmp4j" % "2.2.1",
  "org.snmp4j" % "snmp4j-agent" % "2.0.7" % "test",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

osgiSettings

OsgiKeys.exportPackage := Seq(
  "org.snmp4s"
)

OsgiKeys.privatePackage := Seq()
