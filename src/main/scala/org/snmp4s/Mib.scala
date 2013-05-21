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
  implicit def ReadableScalar2DataObjectWithReadable[T](s:Scalar[T] with ReadWrite[T]):DataObject[T] with ReadableDataObject[T] =
    new DataObjectInst[T](s.oid ++ 0, s.name+".0") with ReadableDataObject[T]
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

trait ReadableDataObject[T] extends DataObject[T]
trait WritableDataObject[T] extends DataObject[T]
trait ReadWriteDataObject[T] extends ReadableDataObject[T] with WritableDataObject[T]

/**
  * Enumerating trait for the MAX-ACCESS property of an OBJECT-TYPE
  */
sealed trait MaxAccess

/**
  * A MIB object with MAX-ACCESS "NoAccess"
  */
trait NoAccess extends MaxAccess

trait AccessibleForNotify extends MaxAccess

/**
  * A MIB object with MAX-ACCESS "Read-only"
  */
trait ReadOnly[T] extends (Oid => ReadableDataObject[T]) with MibObject with MaxAccess {
  def apply(index:Oid) = new DataObjectInst[T](oid ++ index, name+"."+index) with ReadableDataObject[T]
}

/**
  * A MIB object with MAX-ACCESS "ReadWrite"
  */
trait ReadWrite[T] extends (Oid => ReadWriteDataObject[T]) with MibObject with MaxAccess  {
  def apply(index:Oid) = new DataObjectInst[T](oid ++ index, name+"."+index) with ReadWriteDataObject[T]
}

trait WriteOnly[T] extends (Oid => WritableDataObject[T]) with MibObject with MaxAccess {
  def apply(index:Oid) = new DataObjectInst[T](oid ++ index, name+"."+index) with WritableDataObject[T]
}

trait ReadCreate[T] extends (Oid => ReadWriteDataObject[T]) with MibObject with MaxAccess  {
  def apply(index:Oid) = new DataObjectInst[T](oid ++ index, name+"."+index) with ReadWriteDataObject[T]
}

/**
  * A Scalar MIB object.  Place-holder primarily for implicit conversion to allow a
  * scalar to be specified without the default index.  
  * <p>
  * Ex: <br><code>
  * snmp.get(agentppSimMode) 
  * snmp.set(agentppSimMode to 2)
  * </code> 
  */
trait Scalar[T] extends MibObject 

/**
  * Instantiation of the <code>DataObject</code> trait that should suffice for most cases.
  */
class DataObjectInst[T](val oid:Oid, val name:String) extends DataObject[T] 

/**
  * Wrapper of a <code>MibObject</code> and it's respective value for
  * use as a SNMP set request. 
  */
case class VarBind[T](val obj:DataObject[T], val v:T)

