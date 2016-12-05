/*
 * Copyright 2013 org.snmp4s
 * Distributed under the terms of the GNU General Public License v3
 */

package org.snmp4s.gen

import java.io.File

import org.scalatest.{Matchers, WordSpecLike}

class GenSuite extends Gen("org.snmp4s.test.mibs") with WordSpecLike with Matchers {
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
      
      val simMode = name2oid("agentppSimMode")
    }
    
    "generate code for IF-MIB" in {
      def ifMib = load(BuiltIn.IfMib)
      val name2oid = Util.name2oid(ifMib)
      val ifMtu = name2oid("ifMtu")
      val ifDescr = name2oid("ifDescr")
      val ifSpeed = name2oid("ifSpeed")
      val ifLastChange = name2oid("ifLastChange")
      val ifAdminStatus = name2oid("ifAdminStatus")
      
      code(ifDescr) should equal ("""case object IfDescr extends AccessibleObject[ReadOnly, String](Seq(1,3,6,1,2,1,2,2,1,2), "ifDescr", OctetStringSyntax)""")
      code(ifMtu) should equal ("""case object IfMtu extends AccessibleObject[ReadOnly, Int](Seq(1,3,6,1,2,1,2,2,1,4), "ifMtu", IntegerSyntax)""")
      code(ifSpeed) should equal ("""case object IfSpeed extends AccessibleObject[ReadOnly, Int](Seq(1,3,6,1,2,1,2,2,1,5), "ifSpeed", IntegerSyntax)""")
      code(ifLastChange) should equal ("""case object IfLastChange extends AccessibleObject[ReadOnly, Int](Seq(1,3,6,1,2,1,2,2,1,9), "ifLastChange", IntegerSyntax)""")
      
      code(ifAdminStatus) should equal ("""object IfAdminStatus_enum extends EnumInteger {
  type IfAdminStatus = Value
  val Up = Value(1, "up")
  val Down = Value(2, "down")
  val Testing = Value(3, "testing")
}
case object IfAdminStatus extends AccessibleObject[ReadWrite, IfAdminStatus_enum.Value](Seq(1,3,6,1,2,1,2,2,1,7), "ifAdminStatus", IntegerSyntax, Some(IfAdminStatus_enum))""")

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
case object AgentppSimMode extends AccessibleObject[ReadWrite, AgentppSimMode_enum.Value](Seq(1,3,6,1,4,1,4976,2,1,1), "agentppSimMode", IntegerSyntax, Some(AgentppSimMode_enum)) with Scalar[ReadWrite, AgentppSimMode_enum.Value]
case object AgentppSimDeleteRow extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,2), "agentppSimDeleteRow", ObjectIdentifierSyntax) with Scalar[ReadWrite, Int]
case object AgentppSimDeleteTableContents extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,3), "agentppSimDeleteTableContents", ObjectIdentifierSyntax) with Scalar[ReadWrite, Int]

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