package org.snmp4s

import org.scalatest.{WordSpec}
import org.scalatest.matchers.{ShouldMatchers}
import Mib._

class SnmpUnitSuite extends WordSpec with ShouldMatchers {
  val snmp = new Snmp
  case object myNoAccessOid extends MibObjectInst[Int](Seq(1,2,3,4), "myNoAccessOid") with NoAccess
  case object myReadOnlyOid extends MibObjectInst[Int](Seq(1,2,3,4,1), "myReadOnlyOid") with ReadOnly[Int]
  case object myReadWriteOid extends MibObjectInst[Int](Seq(1,2,3,4,2), "myReadWriteOid") with ReadWrite[Int]
  
  "An Snmp" should {
    "compile get() while preserving the value type" in {
      snmp.get(myReadWriteOid) should equal(Right(1))
      snmp.get(myReadOnlyOid)  should equal(Right(1))
    } 
    
    "compile set() while checking the value type" in {
      snmp.set(myReadWriteOid, 5) should equal(None)
    }
  } 
  
  "A MibObject" should {
    "be a function that returns a child" in {
      val child = myReadOnlyOid(0)
      child.oid should equal(Seq(1,2,3,4,1,0))
      myNoAccessOid(1) should equal(myReadOnlyOid)
    }
  }  
  
  
}