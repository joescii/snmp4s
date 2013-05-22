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
      import ifAdminStatus_syntax._
      
      ifAdminStatus_syntax(1) should equal (up)
      ifAdminStatus_syntax(2) should equal (down)
      ifAdminStatus_syntax(3) should equal (test)
      
      up.toString   should equal ("up(1)")
      down.toString should equal ("down(2)")
      test.toString should equal ("test(3)")
      
      ifAdminStatus_syntax(1) match {
        case ifAdminStatus_syntax.up =>
        case ifAdminStatus_syntax.down =>
        case ifAdminStatus_syntax.test =>
      }
    }
  }
}