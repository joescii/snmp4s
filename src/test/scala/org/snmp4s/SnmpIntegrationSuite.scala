package org.snmp4s

import java.io.File
import org.scalatest.{WordSpec, BeforeAndAfter}
import org.scalatest.matchers.{ShouldMatchers}
import Mib._

class SnmpIntegrationSuite extends WordSpec with ShouldMatchers with BeforeAndAfter {
  val snmp = new Snmp
  case object agentppSimMode extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,1), "agentppSimMode") with Scalar[ReadWrite] 
  
  case object ifIndex        extends AccessibleObject[ReadOnly, Int](Seq(1,3,6,1,2,1,2,2,1,1), "ifIndex")
  
  var ta:Option[TestAgent] = None
  
  before {
    ta = Some(TestAgent.start("127.0.0.1/161"))
  }
  
  after {
    ta map ( _.stop )
    ta = None
  }
  
  "An Snmp" should {
    "be able to read value 1 from agentppSimMode on our simulator" in {
      snmp.get(agentppSimMode(0)) should equal (Right(1))
    }
    
    "be able to set value 2 on agentppSimMode, read it back, and set it back to 1 on our simulator" in {
      snmp.get(agentppSimMode(0)) should equal (Right(1))
      snmp.set(agentppSimMode(0) to 2) should equal (None)
      snmp.get(agentppSimMode(0)) should equal (Right(2))
      snmp.set(agentppSimMode(0) to 1) should equal (None)
      snmp.get(agentppSimMode(0)) should equal (Right(1))
    }
    
    "be able to walk something" in {
      snmp.walk(ifIndex) should equal (Left("Crap")) // (Right(Seq(ifIndex(1) vb 1)))
    }
  }
}