/*
 * Copyright 2013 org.snmp4s
 * Distributed under the terms of the GNU General Public License v3
 */

package org.snmp4s.gen

import org.scalatest.{WordSpec}
import org.scalatest.matchers.{ShouldMatchers}
import java.io.File
import scala.collection.JavaConversions._
import net.percederberg.mibble._

class GenSuite extends Gen("org.snmp4s.test.mibs") with WordSpec with ShouldMatchers {
  "A Generator" should {
    "Load agent mib" in {
      val ms = load(new File("src/test/mibs"))
      val name2mib = ms.map { m => m.getName -> m }.toMap

      ms.map { _ getName }.toSet should equal (Set(
        "AGENTPP-GLOBAL-REG",
        "AGENTPP-SIMULATION-MIB"
      ))
      
      val sim = name2mib.get("AGENTPP-SIMULATION-MIB")
      sim.isDefined should equal(true)
      val name2oid = Util.name2oid(sim.get)
      
      name2oid.keySet should equal (Set(
        "agentppSimMIB",
        "agentppSim",
        "agentppSimMode",
        "agentppSimDeleteRow",
        "agentppSimDeleteTableContents",
        "agentppSimBasicGroup",
        "agentppSimCompliance"
      ))
      
      val simMode = name2oid.get("agentppSimMode").get
    }
    
    "generate code for IF-MIB" in {
      def ifMib = load(BuiltIn.IfMib)
      val name2oid = Util.name2oid(ifMib)
      val ifMtu = name2oid.get("ifMtu").get
      val ifDescr = name2oid.get("ifDescr").get
      val ifSpeed = name2oid.get("ifSpeed").get
      val ifLastChange = name2oid.get("ifLastChange").get
      val ifAdminStatus = name2oid.get("ifAdminStatus").get
      
      code(ifDescr) should equal ("""case object IfDescr extends AccessibleObject[ReadOnly, String](Seq(1,3,6,1,2,1,2,2,1,2), "ifDescr")""")
      code(ifMtu) should equal ("""case object IfMtu extends AccessibleObject[ReadOnly, Int](Seq(1,3,6,1,2,1,2,2,1,4), "ifMtu")""")
      code(ifSpeed) should equal ("""case object IfSpeed extends AccessibleObject[ReadOnly, Int](Seq(1,3,6,1,2,1,2,2,1,5), "ifSpeed")""")
      code(ifLastChange) should equal ("""case object IfLastChange extends AccessibleObject[ReadOnly, Int](Seq(1,3,6,1,2,1,2,2,1,9), "ifLastChange")""")
      
      code(ifAdminStatus) should equal ("""object IfAdminStatus_enum extends EnumInteger {
  type IfAdminStatus = Value
  val Up = Value(1, "up")
  val Down = Value(2, "down")
  val Testing = Value(3, "testing")
}
case object IfAdminStatus extends AccessibleObject[ReadWrite, IfAdminStatus_enum.Value](Seq(1,3,6,1,2,1,2,2,1,7), "ifAdminStatus", Some(IfAdminStatus_enum))""")

    }
    
    "generate code for agent MIB" in {
      val ms = load(new File("src/test/mibs"))
      val name2mib = ms.map { m => m.getName -> m }.toMap
      val sim = name2mib.get("AGENTPP-SIMULATION-MIB")
      sim.isDefined should equal(true)
      
      code(sim.get) should equal (
"""package org.snmp4s.test.mibs.AgentppSimulationMib
import org.snmp4s._

object AgentppSimMode_enum extends EnumInteger {
  type AgentppSimMode = Value
  val Oper = Value(1, "oper")
  val Config = Value(2, "config")
}
case object AgentppSimMode extends AccessibleObject[ReadWrite, AgentppSimMode_enum.Value](Seq(1,3,6,1,4,1,4976,2,1,1), "agentppSimMode", Some(AgentppSimMode_enum)) with Scalar[ReadWrite, AgentppSimMode_enum.Value]
case object AgentppSimDeleteRow extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,2), "agentppSimDeleteRow") with Scalar[ReadWrite, Int]
case object AgentppSimDeleteTableContents extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,3), "agentppSimDeleteTableContents") with Scalar[ReadWrite, Int]

""")
    }
    
    "generate code for all built in mibs" in {
      BuiltIn.values map { mib =>
        try {
          code(load(mib))
        } catch {
          case e: Exception => fail("Exception was thrown while generating "+mib)
        }
      }
      
    }
  }
}