/*
 * Copyright 2013 org.snmp4s
 * Distributed under the terms of the GNU General Public License v3
 */

package org.snmp4s.gen

import java.util.jar.JarFile
import scala.collection.JavaConversions._
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.InputStream
import java.io.PrintStream
/**
  * Generates the BuiltIn object because there are so many MIBs included with Mibble 
  */
object GenBuiltIn extends App {
  val src = if(args.length > 0) args(0) else "gen/lib/mibble-mibs-2.9.3.jar"
  val dst = if(args.length > 1) args(1) else "gen/src/main/scala/org/snmp4s/gen/BuiltIn.scala"
    
  val srcFile = new File(src)
  val dstFile = new File(dst)
  
  require(srcFile.isFile())
  require(srcFile.canRead())
  require(dstFile.isFile())
  require(dstFile.canWrite())
  
  val jar = new JarFile(srcFile)
  
  import Console. {println => p}
  
  p(jar.getName())
  
  val body = for {
    e <- jar.entries.toList
    if e.getName().startsWith("mibs")
    if !e.isDirectory()
  } yield {
    import AsciiStream._
    val Pattern = "\\s*([^\\s]+)\\s+DEFINITIONS\\s+::=\\s+BEGIN".r
    val line = jar.getInputStream(e).lines.find(Pattern.findFirstIn(_).isDefined).get
    line match {
      case Pattern(mib) => 
        val camel = mib.split("-").map(s => s.substring(0, 1).toUpperCase + s.substring(1).toLowerCase).mkString
        val code = s"""  val $camel = "$mib""""
        code
      case _ => ""
    }
  }
  
  val code = 
"""package org.snmp4s.gen

/**
  * MIBs that are built into this library complements of Mibble.
  * 
  */
object BuiltIn {
"""+body.mkString("\n")+"\n}"

  val ps = new PrintStream(dstFile)
  ps.println(code)
  ps.close
}

object AsciiStream {
  implicit def Input2Ascii(s:InputStream):AsciiStream = new AsciiStream(s)
}

class AsciiStream(s:InputStream) {
  def lines = {
    val r = new BufferedReader(new InputStreamReader(s))
    Stream.continually(r.readLine()).takeWhile(_ != null)
  }
}