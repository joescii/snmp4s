package org.snmp4s

import org.scalatest.{WordSpec}
import org.scalatest.matchers.{ShouldMatchers}
import org.snmp4j.{Snmp => Snmp4j}
import Mib._

class SnmpUnitSuite extends WordSpec with ShouldMatchers {
  import TestMibs._
  
  "A MibObject" should {
    "be a function that returns a child" in {
      val child = MyReadOnlyOid(0)
      child.oid should equal(Seq(1,2,3,4,1,0))
    }
  }  
  
  "A TextualConvention" should {
    "work" in {
      import IfAdminStatus_enum._
      
      IfAdminStatus_enum(1) should equal (Up)
      IfAdminStatus_enum(2) should equal (Down)
      IfAdminStatus_enum(3) should equal (Test)
      
      Up.toString   should equal ("up")
      Down.toString should equal ("down")
      Test.toString should equal ("test")
      
      Up.id   should equal (1)
      Down.id should equal (2)
      Test.id should equal (3)
      IfAdminStatus_enum.values.contains(Up) should equal (true)
      
      IfAdminStatus_enum(1) match {
        case Up =>
        case Down =>
        case Test =>
      }
    }    
  }
  
  "A VarBind" should {
    "implicitly convert to and from a tuple" in {
      val vb = VarBind(IfIndex(1), 1)
      val t = (IfIndex(1), 1)
      val ivb: VarBind[ReadOnly, Int] = t
      val it: (DataObject[ReadOnly, Int], Int) = vb

      vb should equal(ivb)
      t should equal(it)
      t should equal(vb.tuple)
    }
  }
}