name := "snmp4s-example"

organization := "org.snmp4s"

version := "0.0.1"

scalaVersion := "2.10.1"

scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions")

snmp4sSettings

libraryDependencies ++= Seq(
  "org.snmp4s" %% "snmp4s-core" % "0.1.0"
)

