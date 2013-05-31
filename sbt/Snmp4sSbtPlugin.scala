import sbt._
import sbt.Keys._
import org.snmp4s.gen._

object Snmp4sSbtPlugin extends Plugin
{
  val snmp4sBuiltInMibs = SettingKey[Seq[BuiltIn.Value]]("snmp4s-built-in-mibs")
  val snmp4sMibPackage  = SettingKey[String]("snmp4s-mib-package")

  val snmp4sSettings = Seq(
    snmp4sBuiltInMibs := Seq(),
    snmp4sMibPackage := "org.snmp4s.mib",
    sourceGenerators in Compile <+= (sourceManaged in Compile, snmp4sBuiltInMibs, snmp4sMibPackage) map { (outDir: File, mibs: Seq[BuiltIn.Value], pkg: String) =>
      genMibs(outDir / "mibs", mibs, pkg)
    }
  )


  def genMibs(dst:File, mibs:Seq[BuiltIn.Value], pkg:String) = { 
    val g = new Gen(pkg)
    val dir = pkg.split("\\.").foldLeft(dst){ case (d, next) => d / next }

    mibs map { mib => 
      val file = dir / (org.snmp4s.gen.Util.camel(mib.toString)+".scala")
      IO.write(file, g.code(mib))
      file 
    }
  }
}
