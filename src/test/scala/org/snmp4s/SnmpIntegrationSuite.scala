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
      get(agentppSimMode(0)) should equal (Right(1))
    }
    
    "be able to set value 2 on Read-Write OID agentppSimMode, read it back, and set it back to 1 on our simulator" in {
      get(agentppSimMode) should equal (Right(1))
      set(agentppSimMode to 2) should equal (None)
      get(agentppSimMode) should equal (Right(2))
      set(agentppSimMode to 1) should equal (None)
      get(agentppSimMode) should equal (Right(1))
    }
    
    "be able to get Read-Only OID ifIndex.1" in {
      get(ifIndex(1)) should equal (Right(1))
    }
    
    "be able to get String syntax OID ifDescr" in {
      get(ifDescr(1)) should equal (Right("eth0"))
      get(ifDescr(2)) should equal (Right("loopback"))
    }
    
    "be able to walk on Read-Only OID ifIndex" in {
      walk(ifIndex) should equal (Right(Seq(
        ifIndex(1) vb 1,
        ifIndex(2) vb 2
      )))
    }
    
    "be able to walk on Read-Write OID ifAdminStatus" in {
      walk(ifAdminStatus) should equal (Right(Seq(
        ifAdminStatus(1) vb 1,
        ifAdminStatus(2) vb 1
      )))
    }
    
    "be able to walk on String syntax OID ifDescr" in {
      walk(ifDescr) should equal (Right(Seq(
        ifDescr(1) vb "eth0",
        ifDescr(2) vb "loopback"
      )))
    }
    
    "be able to set String syntax Read-Write OID ifAlias" in {
      get(ifAlias(1)) should equal (Right("My eth"))
      set(ifAlias(1) to "Your eth") should equal (None)
      get(ifAlias(1)) should equal (Right("Your eth"))
    }
  }
}