package org.snmp4s

import org.scalatest.{WordSpec}
import org.scalatest.matchers.{ShouldMatchers}
import org.snmp4j.{Snmp => Snmp4j}
import Mib._

class SnmpUnitSuite extends WordSpec with ShouldMatchers {
  case object myReadOnlyOid extends AccessibleObject[ReadOnly,Int](Seq(1,2,3,4,1), "myReadOnlyOid") with Scalar[ReadOnly] 
  case object myReadWriteOid extends AccessibleObject[ReadWrite, Int](Seq(1,2,3,4,2), "myReadWriteOid") with Scalar[ReadWrite] 
  
  "A MibObject" should {
    "be a function that returns a child" in {
      val child = myReadOnlyOid(0)
      child.oid should equal(Seq(1,2,3,4,1,0))
    }
  }  
  
  
}