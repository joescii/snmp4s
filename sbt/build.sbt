sbtPlugin := true

name := "snmp4s-sbt"

organization := "org.snmp4s"

version := "0.1.0"

scalaVersion := "2.9.2"

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "org.snmp4s" %% "snmp4s-gen" % "0.1.0"
)

