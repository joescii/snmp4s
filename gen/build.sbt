name := "snmp4s-gen"

organization := "org.snmp4s"

version := "0.1.0"

scalaVersion := "2.9.2"

scalacOptions := Seq("-deprecation", "-unchecked")

resolvers += "Mibble" at "http://maven.cloudhopper.com/repos/third-party/"

libraryDependencies ++= Seq(
  "snmp" % "mibble-parser" % "2.9.2",
  "snmp" % "mibble-mibs" % "2.9.2",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

