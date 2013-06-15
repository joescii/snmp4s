/*
 * Copyright 2013 org.snmp4s
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is 
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.snmp4s

import java.io.File
import org.scalatest.{WordSpec, BeforeAndAfter}
import org.scalatest.matchers.{ShouldMatchers}
import Mib._

class SnmpIntegrationSuite extends WordSpec with ShouldMatchers with BeforeAndAfter {
  val snmp = new SnmpSync(SnmpParams())
  
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
  import IfMib._
  import AgentppSimulationMib._
  import scala.util. {Right => V, Left => E}
  import org.snmp4s. {SingleGetResponse => S}
    
  
  "A synchronous Snmp" should {
//    "be able to get Read-Only OID ifIndex.1" in {
//      get(IfIndex(1)) should equal (Right(1))
//    }
//    
//    "be able to get String syntax OID ifDescr" in {
//      get(IfDescr(1)) should equal (Right("eth0"))
//      get(IfDescr(2)) should equal (Right("loopback"))
//    }
//    
    "be able to walk on Read-Only OID ifIndex" in {
      walk(IfIndex) should equal (Right(Seq(
        IfIndex(1) vb 1,
        IfIndex(2) vb 2
      )))
    }
    
    "be able to walk on Read-Write enum OID ifAdminStatus" in {
      import IfAdminStatus_enum._
      walk(IfAdminStatus) should equal (Right(Seq(
        IfAdminStatus(1) vb Up,
        IfAdminStatus(2) vb Up
      )))
    }
    
    "be able to walk on String syntax OID ifDescr" in {
      walk(IfDescr) should equal (Right(Seq(
        IfDescr(1) vb "eth0",
        IfDescr(2) vb "loopback"
      )))
    }

    "be able to pattern match against an OID" in {
      import IfAdminStatus_enum._
      set(IfAdminStatus(2) to Testing)
      walk(IfAdminStatus) match {
        case Right(walk) => {
          val testPorts = for(VarBind(IfAdminStatus(Seq(i)), Testing) <- walk) yield i
          
          testPorts should equal (Seq(2))
        }
        
        case Left(err) => fail
      }

    }
    
    "handle errors" in {
      import IfAdminStatus_enum._
      get(MyReadOnlyOid(1)) should equal (V(S(E(NoSuchName))))
      set(MyReadWriteOid(2) to 42) should equal (Some(NoSuchName))
      walk(MyReadOnlyOid) should equal(Right(Seq()))

      // The WrongValue is the best we can do with SNMP4J. Even though there are 2 OIDs which have
      // errored, SNMP4J only reports 1.
      get(IfAdminStatus(1) &: MyReadOnlyOid(1) &: IfAdminStatus(2) &: IfAdminStatus(3)) should equal (
        Right(Right(Up) &: Left(NoSuchName) &: Right(Up) &: S(Left(WrongValue)))
      )
      get(IfAdminStatus(3)) should equal (V(S(E(NoSuchName))))
      
      val unresolvedName = new SnmpSync(SnmpParams("invalid"))
      unresolvedName.walk(IfAdminStatus) should equal (Left(AgentUnknown))
      unresolvedName.get(IfAdminStatus(1)) should equal (Left(AgentUnknown))
      unresolvedName.set(IfAdminStatus(2) to Testing) should equal (Some(AgentUnknown))
      
      ta map { _ stop }; ta = None      
      
      walk(IfAdminStatus) should equal (Left(AgentUnreachable))
      get(IfAdminStatus(1)) should equal (Left(AgentUnreachable))
      set(IfAdminStatus(2) to Testing) should equal (Some(AgentUnreachable))
    }
    
    "do a get of an integer, octet string, and enum typed objects" in {
      import IfOperStatus_enum._
      val get1:Either[SnmpError,GetResponse[Int]] = get(IfIndex(1)) 
      get1 should equal (V(SingleGetResponse(V(1))))
      val get3 = get(IfIndex(1) &: IfAlias(1) &: IfOperStatus(1))

      get3 match {
        case V(V(v1) &: V(v2) &: S(V(v3))) => 
          v1 should equal (1)
          v2 should equal ("My eth")
          v3 should equal (Up)
        case _ => fail("did not return correct values")
      } 
    }
    
    "do a get of a scalar" in {
      import AgentppSimMode_enum._
      
      get(AgentppSimMode) should equal (V(S(V(Oper))))
    }
    
    "do a set with one VarBind" in {
      val set1:VarBind[ReadWrite,String] = IfAlias(1) to "Set Test"
      set(set1) match {
        case Some(e) => fail("SNMP set failed")
        case _ =>
      }

      get(IfAlias(1)) match {
        case V(S(V(v))) => v should equal ("Set Test")
        case _ =>
      }
    }
    
    "do a set with two VarBinds" in {
      import IfAdminStatus_enum._
      set((IfAlias(1) to "Set Test2") &: (IfAdminStatus(1) to Testing)) match {
        case Some(e) => fail("SNMP set failed")
        case _ =>
      }
      
      get(IfAlias(1) &: IfAdminStatus(1)) match {
        case V(V(alias) &: S(V(status))) =>
          alias should equal ("Set Test2")
          status should equal (Testing)
        case _ => fail("Could not retrieve values")
      }
    }
  }
}