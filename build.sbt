name := "snmp4s"

version := "0.0.1"

scalaVersion := "2.10.1"

scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions")

resolvers += "OO SNMP" at "https://oosnmp.net/dist/release/"

libraryDependencies ++= Seq(
  "org.snmp4j" % "snmp4j" % "2.2.1",
  "org.snmp4j" % "snmp4j-agent" % "2.0.7" % "test",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)
