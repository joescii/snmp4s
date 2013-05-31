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
  val snmp = new Snmp(SnmpParams())
  
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
  
  "An Snmp" should {
    "be able to read value 1 from agentppSimMode on our simulator" in {
      import AgentppSimMode_enum._
      get(AgentppSimMode(0)) should equal (Right(Oper))
    }
    
    "be able to set value 2 on Read-Write OID agentppSimMode, read it back, and set it back to 1 on our simulator" in {
      import AgentppSimMode_enum._
      get(AgentppSimMode) should equal (Right(Oper))
      set(AgentppSimMode to Config) should equal (None)
      get(AgentppSimMode) should equal (Right(Config))
      set(AgentppSimMode to Oper) should equal (None)
      get(AgentppSimMode) should equal (Right(Oper))
    }
    
    "be able to get Read-Only OID ifIndex.1" in {
      get(IfIndex(1)) should equal (Right(1))
    }
    
    "be able to get String syntax OID ifDescr" in {
      get(IfDescr(1)) should equal (Right("eth0"))
      get(IfDescr(2)) should equal (Right("loopback"))
    }
    
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
    
    "be able to get/set an enumerated value" in {
      import IfAdminStatus_enum._
      
      get(IfAdminStatus(1)) should equal (Right(Up))
      set(IfAdminStatus(1) to Down) should equal (None)
      get(IfAdminStatus(1)) should equal (Right(Down))
      set(IfAdminStatus(1) to Up) should equal (None)
      get(IfAdminStatus(1)) should equal (Right(Up))
    }
    
    "be able to walk on String syntax OID ifDescr" in {
      walk(IfDescr) should equal (Right(Seq(
        IfDescr(1) vb "eth0",
        IfDescr(2) vb "loopback"
      )))
    }
    
    "be able to set String syntax Read-Write OID ifAlias" in {
      get(IfAlias(1)) should equal (Right("My eth"))
      set(IfAlias(1) to "Your eth") should equal (None)
      get(IfAlias(1)) should equal (Right("Your eth"))
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
      get(MyReadOnlyOid(1)) should equal (Left(NoSuchName))
      set(MyReadWriteOid(2) to 42) should equal (Some(NoSuchName))
      walk(MyReadOnlyOid) should equal(Right(Seq()))
      
      val unresolvedName = new Snmp(SnmpParams("invalid"))
      unresolvedName.walk(IfAdminStatus) should equal (Left(AgentUnknown))
      unresolvedName.get(IfAdminStatus(1)) should equal (Left(AgentUnknown))
      unresolvedName.set(IfAdminStatus(2) to Testing) should equal (Some(AgentUnknown))
      
      ta map { _ stop }; ta = None      
      
      walk(IfAdminStatus) should equal (Left(AgentUnreachable))
      get(IfAdminStatus(1)) should equal (Left(AgentUnreachable))
      set(IfAdminStatus(2) to Testing) should equal (Some(AgentUnreachable))
    }
  }
}