import sbt._
import org.snmp4s.gen._

object Snmp4sSbtPlugin extends Plugin
{
  val genMibsTask = TaskKey[Unit]("snmp4s-gen-mibs")
  val builtInMibsSetting = SettingKey[Seq[BuiltIn.Value]]("snmp4s-built-in-mibs")

  val snmp4sSettings = Seq(
    builtInMibsSetting := Seq(BuiltIn.IfMib),
    genMibsTask <<= builtInMibsSetting map { mibs => 
      println("Compiling " + mibs)
      mibs map { mib => 
        val g = new Gen
        println(g.code("org.snmp4s.mib", g.load(mib))) 
      }
    }
  )

}
