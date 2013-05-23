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
  
  case object AgentppSimMode extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,1), "agentppSimMode") with Scalar[ReadWrite, Int] 
  case object IfIndex        extends AccessibleObject[ReadOnly, Int](Seq(1,3,6,1,2,1,2,2,1,1), "ifIndex")
  case object IfDescr		 extends AccessibleObject[ReadOnly, String](Seq(1,3,6,1,2,1,2,2,1,2), "ifDescr")
  case object IfAdminStatus  extends AccessibleObject[ReadWrite, IfAdminStatus_enum.Value]   (Seq(1,3,6,1,2,1,2,2,1,7), "ifAdminStatus") { override def enum() = Some(IfAdminStatus_enum) }
  case object IfAlias        extends AccessibleObject[ReadWrite, String](Seq(1,3,6,1,2,1,31,1,1,18), "ifAlias")
}