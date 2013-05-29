import sbt._
import org.snmp4s.gen._

object Snmp4sSbtPlugin extends Plugin
{
  val genMibsTask = TaskKey[Unit]("snmp4s-gen-mibs")
  val builtInMibsSetting = SettingKey[String]("snmp4s-built-in-mibs")

  val snmp4sSettings = Seq(
    builtInMibsSetting := "IfMib",
    genMibsTask <<= builtInMibsSetting map { mib => 
      println("Compiling " + mib)
    }
  )

}
