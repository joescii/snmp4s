/*
 * Copyright 2013 org.snmp4s
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is 
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.snmp4s

import org.scalatest.{WordSpec}
import org.scalatest.matchers.{ShouldMatchers}
import org.snmp4j.{Snmp => Snmp4j}
import Mib._

class SnmpUnitSuite extends WordSpec with ShouldMatchers {
  import TestMibs._
  import IfMib._
  
  "A MibObject" should {
    "be a function that returns a child" in {
      val child = MyReadOnlyOid(0)
      child.oid should equal(Seq(1,2,3,4,1,0))
    }
    
    "have a good toString()" in {
      MyReadOnlyOid.toString should equal ("myReadOnlyOid")
      MyReadOnlyOid(0).toString should equal("myReadOnlyOid.0")
    }
  }  
  
  "A TextualConvention" should {
    "work" in {
      import IfAdminStatus_enum._
      
      IfAdminStatus_enum(1) should equal (Up)
      IfAdminStatus_enum(2) should equal (Down)
      IfAdminStatus_enum(3) should equal (Testing)
      
      Up.toString      should equal ("up")
      Down.toString    should equal ("down")
      Testing.toString should equal ("testing")
      
      Up.id      should equal (1)
      Down.id    should equal (2)
      Testing.id should equal (3)
      IfAdminStatus_enum.values.contains(Up) should equal (true)
      
      IfAdminStatus_enum(1) match {
        case Up =>
        case Down =>
        case Testing =>
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