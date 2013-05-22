package org.snmp4s

object TestMibs {
  case object myReadOnlyOid extends AccessibleObject[ReadOnly,Int](Seq(1,2,3,4,1), "myReadOnlyOid") with Scalar[ReadOnly, Int] 
  case object myReadWriteOid extends AccessibleObject[ReadWrite, Int](Seq(1,2,3,4,2), "myReadWriteOid") with Scalar[ReadWrite, Int] 

  object ifAdminStatus_enum extends EnumInteger {
    type ifAdminStatus = Value
    val up = Value(1, "up")
    val down = Value(2, "down")
    val test = Value(3, "test")
  }
  
  case object agentppSimMode extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,1), "agentppSimMode") with Scalar[ReadWrite, Int] 
  case object ifIndex        extends AccessibleObject[ReadOnly, Int](Seq(1,3,6,1,2,1,2,2,1,1), "ifIndex")
  case object ifDescr		 extends AccessibleObject[ReadOnly, String](Seq(1,3,6,1,2,1,2,2,1,2), "ifDescr")
  case object ifAdminStatus  extends AccessibleObject[ReadWrite, ifAdminStatus_enum.Value]   (Seq(1,3,6,1,2,1,2,2,1,7), "ifAdminStatus") { override def enum() = Some(ifAdminStatus_enum) }
  case object ifAlias        extends AccessibleObject[ReadWrite, String](Seq(1,3,6,1,2,1,31,1,1,18), "ifAlias")
}