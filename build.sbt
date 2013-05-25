name := "snmp4s"

version := "0.1.0"

scalaVersion in Global := "2.10.1"

scalacOptions in Global := Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions")

resolvers in Global += "OO SNMP" at "https://oosnmp.net/dist/release/"

