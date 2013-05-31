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
    sourceGenerators in Compile <+= (sourceManaged in Compile, sourceDirectory in Compile, snmp4sBuiltInMibs, snmp4sMibPackage, streams) map { 
      (outDir:File, srcDir:File, mibs:Seq[BuiltIn.Value], pkg:String, s:TaskStreams) =>
        genMibs(outDir / "mibs", srcDir / "mibs", mibs, pkg, s)
    }
  )


  def genMibs(dst:File, src:File, mibs:Seq[BuiltIn.Value], pkg:String, s:TaskStreams) = { 
    val g = new Gen(pkg)
    val dir = pkg.split("\\.").foldLeft(dst){ case (d, next) => d / next }
    val mibFileCount = if(src.list != null) src.list.size else 0
    val mibCount = mibs.size + mibFileCount

    s.log.info("Generating Scala source from "+mibCount+" MIB"+(if(mibCount == 1) "" else "s")+" to "+dst+"...")

    val fileCode = g.code(src) map { case (name, code) =>
      val file = dir / (org.snmp4s.gen.Util.camel(name)+".scala")
      IO.write(file, code)
      file
    }

    val builtInCode = mibs map { mib => 
      val file = dir / (org.snmp4s.gen.Util.camel(mib.toString)+".scala")
      IO.write(file, g.code(mib))
      file 
    }

    (fileCode ++ builtInCode).toSeq
  }
}
