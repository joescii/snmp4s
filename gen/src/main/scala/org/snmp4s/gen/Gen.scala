/*
 * Copyright 2013 org.snmp4s
 * Distributed under the terms of the GNU General Public License v3
 */

package org.snmp4s.gen

import net.percederberg.mibble._
import net.percederberg.mibble.snmp._
import java.io.File
import scala.collection.JavaConversions._

protected object Util {
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
}

class Gen {
  def load(name:String):Mib = {
    val loader = new MibLoader
    loader load name
  }
  
  def load(file:File):Seq[Mib] = {
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
  
  def code(oid:MibValueSymbol):String = {
    val name = oid.getName
    val objName = name.substring(0, 1).toUpperCase() + name.substring(1)
    if(oid.getType.isInstanceOf[SnmpObjectType]) {
      val snmp = oid.getType.asInstanceOf[SnmpObjectType]
      val access = accessMap.get(snmp.getAccess()).get
      val octets = oid.getValue.toString.replace(".", ",")
      val syntax = syntaxMap.get(snmp.getSyntax().getName()).get
            
      val code = s"""case object $objName extends AccessibleObject[$access, $syntax](Seq($octets), "$name")"""
      if(oid.isScalar) code + s" with Scalar[$access, $syntax]"
      else code
    } else {
      ""
    }
  }
  
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
    "OCTET STRING" -> "String"
  )
}