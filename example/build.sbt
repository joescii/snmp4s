name := "snmp4s-example"

version := "0.0.1"

scalaVersion := "2.10.1"

scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions")

snmp4sSettings

resolvers ++= Seq(
  "Mibble" at "http://maven.cloudhopper.com/repos/third-party/",
  "OO SNMP" at "https://oosnmp.net/dist/release/"
)

libraryDependencies ++= Seq(
  "snmp" % "mibble-parser" % "2.9.2",
  "snmp" % "mibble-mibs" % "2.9.2"
)

