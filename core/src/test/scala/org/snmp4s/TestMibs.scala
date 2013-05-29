/*
 * Copyright 2013 org.snmp4s
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is 
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.snmp4s

object TestMibs {
  case object MyReadOnlyOid extends AccessibleObject[ReadOnly,Int](Seq(1,2,3,4,1), "myReadOnlyOid") with Scalar[ReadOnly, Int] 
  case object MyReadWriteOid extends AccessibleObject[ReadWrite, Int](Seq(1,2,3,4,2), "myReadWriteOid") with Scalar[ReadWrite, Int] 

  object IfAdminStatus_enum extends EnumInteger {
    type IfAdminStatus = Value
    val Up = Value(1, "up")
    val Down = Value(2, "down")
    val Test = Value(3, "test")
  }
  
  object AgentppSimMode_enum extends EnumInteger {
    type AgentppSimMode = Value
    val Oper = Value(1, "oper")
    val Config = Value(2, "config")
  }
  case object AgentppSimMode extends AccessibleObject[ReadWrite, AgentppSimMode_enum.Value](Seq(1,3,6,1,4,1,4976,2,1,1), "agentppSimMode", Some(AgentppSimMode_enum)) with Scalar[ReadWrite, AgentppSimMode_enum.Value]
  case object AgentppSimDeleteRow extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,2), "agentppSimDeleteRow") with Scalar[ReadWrite, Int]
  case object AgentppSimDeleteTableContents extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,3), "agentppSimDeleteTableContents") with Scalar[ReadWrite, Int]
  
  case object IfIndex        extends AccessibleObject[ReadOnly, Int](Seq(1,3,6,1,2,1,2,2,1,1), "ifIndex")
  case object IfDescr		 extends AccessibleObject[ReadOnly, String](Seq(1,3,6,1,2,1,2,2,1,2), "ifDescr")
  case object IfAdminStatus  extends AccessibleObject[ReadWrite, IfAdminStatus_enum.Value]   (Seq(1,3,6,1,2,1,2,2,1,7), "ifAdminStatus", Some(IfAdminStatus_enum)) 
  case object IfAlias        extends AccessibleObject[ReadWrite, String](Seq(1,3,6,1,2,1,31,1,1,18), "ifAlias")
}