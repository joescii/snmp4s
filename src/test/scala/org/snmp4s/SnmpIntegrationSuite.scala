package org.snmp4s

import java.io.File
import org.scalatest.{WordSpec, BeforeAndAfter}
import org.scalatest.matchers.{ShouldMatchers}
import Mib._

class SnmpIntegrationSuite extends WordSpec with ShouldMatchers with BeforeAndAfter {
  val snmp = new Snmp  
  
  var ta:Option[TestAgent] = None
  
  before {
    ta = Some(TestAgent.start("127.0.0.1/161"))
  }
  
  after {
    ta map ( _.stop )
    ta = None
  }
  
  import snmp._
  import TestMibs._
  
  "An Snmp" should {
    "be able to read value 1 from agentppSimMode on our simulator" in {
      get(AgentppSimMode(0)) should equal (Right(1))
    }
    
    "be able to set value 2 on Read-Write OID agentppSimMode, read it back, and set it back to 1 on our simulator" in {
      get(AgentppSimMode) should equal (Right(1))
      set(AgentppSimMode to 2) should equal (None)
      get(AgentppSimMode) should equal (Right(2))
      set(AgentppSimMode to 1) should equal (None)
      get(AgentppSimMode) should equal (Right(1))
    }
    
    "be able to get Read-Only OID ifIndex.1" in {
      get(IfIndex(1)) should equal (Right(1))
    }
    
    "be able to get String syntax OID ifDescr" in {
      get(IfDescr(1)) should equal (Right("eth0"))
      get(IfDescr(2)) should equal (Right("loopback"))
    }
    
    "be able to walk on Read-Only OID ifIndex" in {
      walk(IfIndex) should equal (Right(Seq(
        IfIndex(1) vb 1,
        IfIndex(2) vb 2
      )))
    }
    
    "be able to walk on Read-Write enum OID ifAdminStatus" in {
      import IfAdminStatus_enum._
      walk(IfAdminStatus) should equal (Right(Seq(
        IfAdminStatus(1) vb Up,
        IfAdminStatus(2) vb Up
      )))
    }
    
    "be able to get/set an enumerated value" in {
      import IfAdminStatus_enum._
      
      get(IfAdminStatus(1)) should equal (Right(Up))
      set(IfAdminStatus(1) to Down) should equal (None)
      get(IfAdminStatus(1)) should equal (Right(Down))
      set(IfAdminStatus(1) to Up) should equal (None)
      get(IfAdminStatus(1)) should equal (Right(Up))
    }
    
    "be able to walk on String syntax OID ifDescr" in {
      walk(IfDescr) should equal (Right(Seq(
        IfDescr(1) vb "eth0",
        IfDescr(2) vb "loopback"
      )))
    }
    
    "be able to set String syntax Read-Write OID ifAlias" in {
      get(IfAlias(1)) should equal (Right("My eth"))
      set(IfAlias(1) to "Your eth") should equal (None)
      get(IfAlias(1)) should equal (Right("Your eth"))
    }
    
    "be able to pattern match against a walk" in {
      import IfAdminStatus_enum._
      walk(IfAdminStatus) match {
        case Right(l) => {
          val t = l map { case VarBind(IfAdminStatus(i), v) => (i, v) }
          t should equal (Seq((Seq(1),Up), (Seq(2),Up)))
        }
        
        case Left(err) => fail
      }

    }
  }
}