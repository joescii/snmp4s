import sbt._
import Keys._

object Snmp4s extends Build {
  lazy val root = Project(id = "snmp4s",
                          base = file(".")) aggregate(core, gen, sbtp)

  lazy val core = Project(id = "snmp4s-core",
                          base = file("core"))

  lazy val gen  = Project(id = "snmp4s-gen",
                          base = file("gen"))

  lazy val sbtp = Project(id = "snmp4s-sbt",
                          base = file("sbt"))
}
