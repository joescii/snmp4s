/*
 * Copyright 2013 org.snmp4s
 * Distributed under the terms of the GNU General Public License v3
 */

package org.snmp4s.gen

import net.percederberg.mibble._
import java.io.File

class Gen {
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
  
  
}