import sbt._
import sbt.Keys._
import org.snmp4s.gen._

object Snmp4sSbtPlugin extends Plugin
{
  val builtInMibsSetting = SettingKey[Seq[BuiltIn.Value]]("snmp4s-built-in-mibs")

  val snmp4sSettings = Seq(
    builtInMibsSetting := Seq(BuiltIn.IfMib),
    sourceGenerators in Compile <+= sourceManaged in Compile map { outDir: File =>
      genMibs(outDir / "mibs")
    }
  )


  def genMibs(dst:File): Seq[File] = { 
    val mibs = Seq(BuiltIn.IfMib) 
    println("Compiling " + mibs)
    mibs map { mib => 
      val file = dst / "org" / "snmp4s" / "mib" / "IfMib.scala"
      val g = new Gen
      IO.write(file, g.code("org.snmp4s.mib", g.load(mib)))
      file 
    }
    
  }
}
