package org.snmp4s

import org.scalatest.{WordSpec}
import org.scalatest.matchers.{ShouldMatchers}
import org.snmp4j.{Snmp => Snmp4j}
import Mib._

class SnmpUnitSuite extends WordSpec with ShouldMatchers {
  case object myReadOnlyOid extends Scalar[Int] with ReadOnly[Int] { val oid = Seq(1,2,3,4,1); val name = "myReadOnlyOid" }
  case object myReadWriteOid extends Scalar[Int] with ReadWrite[Int] { val oid = Seq(1,2,3,4,2); val name = "myReadWriteOid" }
  
  "A MibObject" should {
    "be a function that returns a child" in {
      val child = myReadOnlyOid(0)
      child.oid should equal(Seq(1,2,3,4,1,0))
    }
  }  
  
  
}