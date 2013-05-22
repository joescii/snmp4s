package org.snmp4s

object TestMibs {
  case object myReadOnlyOid extends AccessibleObject[ReadOnly,Int](Seq(1,2,3,4,1), "myReadOnlyOid") with Scalar[ReadOnly, Int] 
  case object myReadWriteOid extends AccessibleObject[ReadWrite, Int](Seq(1,2,3,4,2), "myReadWriteOid") with Scalar[ReadWrite, Int] 
  
  sealed trait ifAdminStatus_syntax extends TextualConvention
  case object ifAdminStatus_syntax extends (Int => ifAdminStatus_syntax) with TextualConvention {
    case object up extends ifAdminStatus_syntax with TextualConventionEntry { val enum = 1; val name = "up"; val parent = ifAdminStatus_syntax }
    case object down extends ifAdminStatus_syntax with TextualConventionEntry { val enum = 2; val name = "down"; val parent = ifAdminStatus_syntax }
    case object test extends ifAdminStatus_syntax with TextualConventionEntry { val enum = 3; val name = "test"; val parent = ifAdminStatus_syntax }
    
    def apply(enum:Int):ifAdminStatus_syntax = enum match {
      case 1 => up
      case 2 => down
      case 3 => test
    } 
  }
  
  case object agentppSimMode extends AccessibleObject[ReadWrite, Int](Seq(1,3,6,1,4,1,4976,2,1,1), "agentppSimMode") with Scalar[ReadWrite, Int] 
  case object ifIndex        extends AccessibleObject[ReadOnly, Int](Seq(1,3,6,1,2,1,2,2,1,1), "ifIndex")
  case object ifDescr		 extends AccessibleObject[ReadOnly, String](Seq(1,3,6,1,2,1,2,2,1,2), "ifDescr")
  case object ifAdminStatus  extends AccessibleObject[ReadWrite, Int]   (Seq(1,3,6,1,2,1,2,2,1,7), "ifAdminStatus")
  case object ifAlias        extends AccessibleObject[ReadWrite, String](Seq(1,3,6,1,2,1,31,1,1,18), "ifAlias")
}