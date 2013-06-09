package org.snmp4s
package AgentppSimulationMib

object AgentppSimMode_enum extends EnumInteger {
  type AgentppSimMode = Value
  val Oper = Value(1, "oper")
  val Config = Value(2, "config")
}
case object AgentppSimMode extends AccessibleObject[ReadWrite, AgentppSimMode_enum.Value](Seq(1,3,6,1,4,1,4976,2,1,1), "agentppSimMode", IntegerSyntax, Some(AgentppSimMode_enum)) with Scalar[ReadWrite, AgentppSimMode_enum.Value]
case object AgentppSimDeleteRow extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,2), "agentppSimDeleteRow", ObjectIdentifierSyntax) with Scalar[ReadWrite, Int]
case object AgentppSimDeleteTableContents extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,3), "agentppSimDeleteTableContents", ObjectIdentifierSyntax) with Scalar[ReadWrite, Int]

