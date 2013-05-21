package org.snmp4s

import java.io.File
import org.scalatest.{WordSpec, BeforeAndAfter}
import org.scalatest.matchers.{ShouldMatchers}
import Mib._

class SnmpIntegrationSuite extends WordSpec with ShouldMatchers with BeforeAndAfter {
  val snmp = new Snmp
  case object agentppSimMode extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,1), "agentppSimMode") with Scalar[ReadWrite, Int] 
  case object ifIndex        extends AccessibleObject[ReadOnly, Int](Seq(1,3,6,1,2,1,2,2,1,1), "ifIndex")
  case object ifAdminStatus  extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,2,1,2,2,1,7), "ifAdminStatus")
  
  var ta:Option[TestAgent] = None
  
  before {
    ta = Some(TestAgent.start("127.0.0.1/161"))
  }
  
  after {
    ta map ( _.stop )
    ta = None
  }
  
  import snmp._
  
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
  }
}