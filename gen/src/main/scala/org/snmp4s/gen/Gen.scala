/*
 * Copyright 2013 org.snmp4s
 * Distributed under the terms of the GNU General Public License v3
 */

package org.snmp4s.gen

import net.percederberg.mibble._
import net.percederberg.mibble.snmp._
import net.percederberg.mibble.`type`._
import java.io.File
import scala.collection.JavaConversions._

object Util {
  def name2oid(mib:Mib) = {
    val syms = mib.getAllSymbols()
    (for {
        sym <- syms
        if (sym.isInstanceOf[MibValueSymbol])
      } yield {
        val s = sym.asInstanceOf[MibValueSymbol]
        s.getName -> s
      }).toMap
  }
  
  def camel(mib:String) = mib.split("-").map(s => s.substring(0, 1).toUpperCase + s.substring(1).toLowerCase).mkString
}

class Gen {
  def load(mib:BuiltIn.Value):Mib = {
    val loader = new MibLoader
    loader.load(mib.toString())
  }
  
  def load(file:File):Seq[Mib] = {
    require(file.isDirectory(), "The file must be a directory")
    require(file.canRead(), "The directory must be readable")
    val loader = new MibLoader
    loader.addDir(file)
    
    (for {
      m <- file.listFiles()
      if m isFile
    } yield {
      try {
        Some(loader load m)
      } catch {
        case e: MibLoaderException =>
          e.getLog.printTo(System.err)
          None
      }
    }).flatten.toSeq
  }
  
  def code(pkg:String, mib:Mib):String = {
    val syms = mib.getAllSymbols()
    "package " + pkg + "." + Util.camel(mib.getName()) + "\n" +
    "import org.snmp4s._\n" +
    (for {
        sym <- syms
        if sym.isInstanceOf[MibValueSymbol]
        if sym.asInstanceOf[MibValueSymbol].getChildCount() == 0
      } yield {
        val s = sym.asInstanceOf[MibValueSymbol]
        code(s)
      }).mkString("\n")
  }
  
  private def toObjName(name:String):String = 
    name.split("-").map(s => s.substring(0, 1).toUpperCase + s.substring(1)).mkString
  
  def code(oid:MibValueSymbol):String = {
    val name = oid.getName
    val objName = toObjName(name)
    if(oid.getType.isInstanceOf[SnmpObjectType]) {
      val snmp = oid.getType.asInstanceOf[SnmpObjectType]
      val access = accessMap.get(snmp.getAccess()).get
      val octets = oid.getValue.toString.replace(".", ",")
      val (scalaType, enumArg, typeCode) = syntax(objName, snmp.getSyntax)
      
      val code = typeCode + "case object "+objName+" extends AccessibleObject["+access+", "+scalaType+"](Seq("+octets+"), \""+name+"\""+enumArg+")"
      if(oid.isScalar) code + " with Scalar["+access+", "+scalaType+"]"
      else code
    } else {
      ""
    }
  }
  
  private def syntax(objName:String, syntax:MibType):(String,String,String) = {
    if(syntax.isInstanceOf[IntegerType]) {
      val intType = syntax.asInstanceOf[IntegerType]
      if(intType.hasSymbols) syntaxEnum(objName, intType)
      else syntaxGeneral(syntax)
    }
    else syntaxGeneral(syntax)
  }
  
  private def syntaxEnum(objName:String, syntax:IntegerType):(String,String,String) = {
    val scalaType = objName+"_enum.Value"
    val enumArg = ", Some("+objName+"_enum)"
    
    val typeHead = "object "+objName+"_enum extends EnumInteger {\n"+
    "  type "+objName+" = Value\n"
    val entries = for {
      s <- syntax.getAllSymbols.toList
    } yield {
      val v = s.getValue
      val nl = s.getName
      val nu = toObjName(nl)
      "  val "+nu+" = Value("+v+", \""+nl+"\")\n"
    }
    val typeTail = "}\n"
    
    val typeCode = typeHead + entries.mkString + typeTail
      
    (scalaType, enumArg, typeCode)
  }
  
  private def syntaxGeneral(syntax:MibType):(String,String,String) = (syntaxMap.get(syntax.getName).get, "", "")
  
  private val accessMap = Map(
    SnmpAccess.READ_WRITE -> "ReadWrite",
    SnmpAccess.NOT_ACCESSIBLE -> "NotAccessible",
    SnmpAccess.ACCESSIBLE_FOR_NOTIFY -> "AccessibleForNotify",
    SnmpAccess.READ_CREATE -> "ReadCreate",
    SnmpAccess.READ_ONLY -> "ReadOnly",
    SnmpAccess.WRITE_ONLY -> "WriteOnly"
  )
  
  private val syntaxMap = Map(
    "INTEGER" -> "Int",
    "OCTET STRING" -> "String",
    "OBJECT IDENTIFIER" -> "Int",
    "BITS" -> "Int"
  )
}