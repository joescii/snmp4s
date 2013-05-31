import org.snmp4s.gen.BuiltIn._

name := "snmp4s-example"

organization := "org.snmp4s"

version := "0.0.1"

scalaVersion := "2.10.1"

scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions")

snmp4sSettings

snmp4sBuiltInMibs := Seq(IfMib, AdslLineMib)

snmp4sMibPackage := "org.snmp4s.example.mib"

libraryDependencies ++= Seq(
  "org.snmp4s" %% "snmp4s-core" % "0.1.0"
)

