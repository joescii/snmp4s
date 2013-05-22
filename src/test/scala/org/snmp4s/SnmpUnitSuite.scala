package org.snmp4s

import org.scalatest.{WordSpec}
import org.scalatest.matchers.{ShouldMatchers}
import org.snmp4j.{Snmp => Snmp4j}
import Mib._

class SnmpUnitSuite extends WordSpec with ShouldMatchers {
  import TestMibs._
  
  "A MibObject" should {
    "be a function that returns a child" in {
      val child = myReadOnlyOid(0)
      child.oid should equal(Seq(1,2,3,4,1,0))
    }
  }  
  
  "A TextualConvention" should {
    "work" in {
      import ifAdminStatus_enum._
      
      ifAdminStatus_enum(1) should equal (up)
      ifAdminStatus_enum(2) should equal (down)
      ifAdminStatus_enum(3) should equal (test)
      
      up.toString   should equal ("up")
      down.toString should equal ("down")
      test.toString should equal ("test")
      
      up.id   should equal (1)
      down.id should equal (2)
      test.id should equal (3)
      ifAdminStatus_enum.values.contains(up) should equal (true)
      
      ifAdminStatus_enum(1) match {
        case ifAdminStatus_enum.up =>
        case ifAdminStatus_enum.down =>
        case ifAdminStatus_enum.test =>
      }
    }
  }
}