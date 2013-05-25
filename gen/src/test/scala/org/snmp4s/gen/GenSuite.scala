/*
 * Copyright 2013 org.snmp4s
 * Distributed under the terms of the GNU General Public License v3
 */

package org.snmp4s.gen

import org.scalatest.{WordSpec}
import org.scalatest.matchers.{ShouldMatchers}
import java.io.File

class GenSuite extends WordSpec with ShouldMatchers {
  "A Generator" should {
    "Load agent mib" in {
      val g = new Gen
      val ms = g.load(new File("gen/src/test/mibs"))

      ms.map { _ getName }.toSet should equal (Set(
        "AGENTPP-GLOBAL-REG",
        "AGENTPP-SIMULATION-MIB"
      ))
    }
    
    
  }
}