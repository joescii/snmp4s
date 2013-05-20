package org.snmp4s

import org.snmp4j.mp.SnmpConstants

/**
  * Contains types and implicit conversions handy for SNMP4S 
  */
object Mib {
  
  /**
    * Type declaration for the raw OID, which is an ordered sequence of integers 
    */
  type Oid = Seq[Int]
  
  /**
    * Implicit conversion from a single Int to an Oid. 
    */
  implicit def Int2Oid(i:Int):Oid = Seq(i)
  
  implicit def Tuple2VarBind[T](v:(Writable[T], T)):VarBind[T] = VarBind(v._1, v._2)
}

import Mib._

/**
  * Enumeration of SNMP versions 
  */
sealed trait Version { def enum:Int }
case object Version1  extends Version { override def enum = SnmpConstants.version1 }
case object Version2c extends Version { override def enum = SnmpConstants.version2c }
case object Version3  extends Version { override def enum = SnmpConstants.version3 }

/**
  * An OBJECT-TYPE which is defined in a MIB.   
  */
trait MibObject[T] extends (Oid => MibObject[T]) {
  def oid():Oid
  def name():String

  def canEqual(other: Any) = {
    other.isInstanceOf[MibObject[T]]
  }
  
  override def equals(other: Any) = {
    other match {
      case that: MibObject[T] => that.canEqual(MibObject.this) && oid == that.oid
      case _ => false
    }
  }
  
  override def hashCode() = {
    val prime = 41
    prime + oid.hashCode
  }
}

/**
  * A MIB object which can be read from a remote SNMP agent.
  */
trait Readable[T] extends MibObject[T]

/**
  * A MIB object which can be written to a remote SNMP agent.  
  */
trait Writable[T] extends MibObject[T] {
  
  /**
    * Returns a <code>VarBind</code> to be passed to <code>Snmp.set</code>.
    */
  def to(v:T):VarBind[T] = (this, v)
}

/**
  * Enumerating trait for the MAX-ACCESS property of an OBJECT-TYPE
  */
sealed trait MaxAccess

/**
  * A MIB object with MAX-ACCESS "NoAccess"
  */
trait NoAccess extends MaxAccess

/**
  * A MIB object with MAX-ACCESS "Read-only"
  */
trait ReadOnly[T] extends MaxAccess with Readable[T]

/**
  * A MIB object with MAX-ACCESS "ReadWrite"
  */
trait ReadWrite[T] extends MaxAccess with Readable[T] with Writable[T]

/**
  * Instantiation of the <code>MibObject</code> trait that should suffice for most cases.
  */
class MibObjectInst[T](val oid:Oid, val name:String) extends MibObject[T] with Equals {
  def apply(index:Oid) = new MibObjectInst[T](oid ++ index, name+"."+oid.mkString("."))
  
}

/**
  * Wrapper of a <code>MibObject</code> and it's respective value for
  * use as a SNMP set request. 
  */
case class VarBind[T](val obj:Writable[T], val v:T)

