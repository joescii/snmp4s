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
  
  /**
    * Implicit conversion that allows us to specify VarBinds simply as a tuple of the DataObject
    * and the value.
    */
  implicit def Tuple2VarBind[T](v:(DataObject[T], T)):VarBind[T] = VarBind(v._1, v._2)
  
  /**
    * Implicit conversion that allows us to drop the default index (0) from scalars.
    */
  implicit def Scalar2DataObject[T](s:Scalar[T]):DataObject[T] = 
    new DataObjectInst[T](s.oid ++ 0, s.name+".0")

  /**
    * Implicit conversion that allows us to drop the default index (0) from scalars.
    */
  implicit def ReadableScalar2DataObjectWithReadable[T](s:Scalar[T] with Readable[T]):DataObject[T] with Readable[T] =
    new DataObjectInst[T](s.oid ++ 0, s.name+".0") with Readable[T]
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
trait MibObject extends Equals {
  def oid():Oid
  def name():String

  def canEqual(other: Any) = {
    other.isInstanceOf[MibObject]
  }
  
  override def equals(other: Any) = {
    other match {
      case that: MibObject => that.canEqual(MibObject.this) && oid == that.oid
      case _ => false
    }
  }
  
  override def hashCode() = {
    val prime = 41
    prime + oid.hashCode
  }
}

/**
  * A DataObject is a leaf OID which is complete and can be bound to a variable.
  */
trait DataObject[T] extends MibObject {
  /**
    * Convenience method to create a VarBind with this OID.
    */
  def vb(v:T):VarBind[T] = (this, v)
  
  /**
    * Returns a <code>VarBind</code> to be passed to <code>Snmp.set</code>.  Just a 
    * cosmetic DSL alias for vb.
    */
  def to(v:T):VarBind[T] = vb(v)
}

/**
  * This is an OID that can be completed by adding a single index.  Sketchy, I know...
  */
trait Completeable[T] extends (Int => DataObject[T]) with MibObject 

/**
  * A MIB object which can be read from a remote SNMP agent.
  */
trait Readable[T] extends Completeable[T] {
    def apply(index:Int) = new DataObjectInst[T](oid :+ index, name+"."+index) with Readable[T]
}

/**
  * A MIB object which can be written to a remote SNMP agent.  
  */
trait Writable[T] extends Readable[T] {
  override def apply(index:Int) = new DataObjectInst[T](oid :+ index, name+"."+index) with Writable[T]
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
trait ReadWrite[T] extends MaxAccess with Writable[T]

/**
  * A Scalar MIB object.  Place-holder primarily for implicit conversion to allow a
  * scalar to be specified without the default index.  
  * <p>
  * Ex: <br><code>
  * snmp.get(agentppSimMode) 
  * snmp.set(agentppSimMode to 2)
  * </code> 
  */
trait Scalar[T] extends Completeable[T] 

/**
  * Instantiation of the <code>DataObject</code> trait that should suffice for most cases.
  */
class DataObjectInst[T](val oid:Oid, val name:String) extends DataObject[T] 

/**
  * Wrapper of a <code>MibObject</code> and it's respective value for
  * use as a SNMP set request. 
  */
case class VarBind[T](val obj:DataObject[T], val v:T)

