/*
 * Copyright 2013 org.snmp4s
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is 
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License.
 */

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
  implicit def Tuple2VarBind[A <: MaxAccess, T](v:(DataObject[A, T], T)):VarBind[A, T] = VarBind(v._1, v._2)
  
  implicit def VarBind2Tuple[A <: MaxAccess, T](vb:VarBind[A, T]) = vb.tuple
  
  /**
    * Implicit conversion that allows us to drop the default index (0) from scalars.
    */
  implicit def Scalar2DataObject[A <: MaxAccess, T](s:Scalar[A, T]):DataObject[A, T] = s(0)
  
  implicit def DataObject2GetRequest[A <: Readable, T](obj:DataObject[A, T]):GetRequest[T] = SingleGetRequest(obj)
  
  // This is here for pattern matching with &:  Unfortunately, it doesn't work at the moment.  
  // Should be fixed in Scala 2.11: https://github.com/scala/scala/commit/b92eb70113
  implicit def ValueToGetResponse[T](res:Either[SnmpError, T]):GetResponse[T] = SingleGetResponse(res)
  
  implicit def VarBind2SetRequest[A <: Writable, T](vb:VarBind[A, T]):SetRequest[T] = SingleSetRequest(vb)
}

import Mib._

/**
  * Enumeration of SNMP versions 
  */
sealed trait Version { def enum:Int }
case object Version1  extends Version { override def enum = SnmpConstants.version1 }
case object Version2c extends Version { override def enum = SnmpConstants.version2c }
case object Version3  extends Version { override def enum = SnmpConstants.version3 }

sealed trait Syntax
case object IntegerSyntax extends Syntax
case object OctetStringSyntax extends Syntax
case object ObjectIdentifierSyntax extends Syntax
case object BitsSyntax extends Syntax
case object ChoiceSyntax extends Syntax

trait EnumInteger extends Enumeration

/**
  * An OBJECT-TYPE which is defined in a MIB.   
  */
trait MibObject[A <: MaxAccess] extends Equals {
  val oid:Oid
  val name:String
  val enum:Option[EnumInteger]
  val syntax:Syntax

  def canEqual(other: Any) = {
    other.isInstanceOf[MibObject[A]]
  }
  
  override def equals(other: Any) = {
    other match {
      case that: MibObject[A] => that.canEqual(MibObject.this) && oid == that.oid
      case _ => false
    }
  }
  
  override def hashCode() = {
    val prime = 41
    prime + oid.hashCode
  }
  
  override def toString = name
}

/**
  * A DataObject is a leaf OID which is complete and can be bound to a variable.
  */
trait DataObject[A <: MaxAccess, T] extends MibObject[A] {
  /**
    * Returns a <code>VarBind</code> to be passed to <code>Snmp.set</code>.  Just a 
    * cosmetic DSL alias for vb.
    */
  def vb(v:T) = VarBind(this, v)
  
  /**
    * Convenience method to create a VarBind with this OID.
    */
  def to(v:T) = vb(v)
}

/**
  * Enumerating trait for the MAX-ACCESS property of an OBJECT-TYPE
  */
sealed trait MaxAccess

/**
  * An object with MAX-ACCESS that is readable.
  */
protected trait Readable extends MaxAccess

/**
  * An object with MAX-ACCESS that is writable 
  */
protected trait Writable extends MaxAccess

/**
  * A MIB object with MAX-ACCESS "Not-accessible"
  */
trait NotAccessible extends MaxAccess

/**
  * A MIB object with MAX-ACCESS "Accessible-for-notify"
  */
trait AccessibleForNotify extends MaxAccess

/**
  * A MIB object with MAX-ACCESS "Read-only"
  */
trait ReadOnly extends MaxAccess with Readable

/**
  * A MIB object with MAX-ACCESS "Read-write"
  */
trait ReadWrite extends MaxAccess with Readable with Writable

/**
  * A MIB object with MAX-ACCESS "Write-only"
  */
trait WriteOnly extends MaxAccess with Writable

/**
  * A MIB object with MAX-ACCESS "Read-create"
  */
trait ReadCreate extends MaxAccess with Readable with Writable

/**
  * A MIB object with MAX-ACCESS "Read-only"
  */
abstract class AccessibleObject[A <: MaxAccess, T] (val oid:Oid, val name:String, val syntax:Syntax, val enum:Option[EnumInteger] = None) 
  extends (Oid => DataObject[A, T]) with MibObject[A] {
  def apply(index:Oid) = DataObjectInst[A, T](oid ++ index, name+"."+(index.mkString(".")), syntax, enum) 
  def unapply(obj:DataObjectInst[A, T]):Option[Oid] = Some(obj.oid.last) // <-- TODO: Do this right
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
trait Scalar[A <: MaxAccess, T] extends AccessibleObject[A, T] 

/**
  * Instantiation of the <code>DataObject</code> trait that should suffice for most cases.
  */
case class DataObjectInst[A <: MaxAccess, T](val oid:Oid, val name:String, val syntax:Syntax, val enum:Option[EnumInteger] = None) extends DataObject[A, T]

/**
  * Wrapper of a <code>MibObject</code> and it's respective value for
  * use as a SNMP set request or as a response to a walk.
  */
case class VarBind[A <: MaxAccess, T](val obj:DataObject[A, T], val v:T) {
  /**
    * Convenience value for handling a VarBind as a tuple
    */
  val tuple = (obj, v)
}

sealed trait GetRequest[T] {
  def &:[A <: Readable, U](obj:DataObject[A, U]):GetRequest[(U, T)] = CompoundGetRequest(obj, this)
}
protected case class SingleGetRequest[A <: Readable, T](val obj:DataObject[A, T]) extends GetRequest[T]
case class CompoundGetRequest[A <: Readable, T, U](val head:DataObject[A, T], val t:GetRequest[U]) extends GetRequest[(T, U)]

sealed trait GetResponse[T] {
  def &:[U](res:Either[SnmpError,U]): GetResponse[(U, T)] = org.snmp4s.&:(res, this)
}
protected case class SingleGetResponse[T](val res:Either[SnmpError,T]) extends GetResponse[T]
case class &:[T, U](val head:Either[SnmpError,T], val tail:GetResponse[U]) extends GetResponse[(T, U)]

sealed trait SetRequest[T] {
  def &:[A <: Writable, U](vb:VarBind[A, U]):SetRequest[(U, T)] = CompoundSetRequest(vb, this)
}
protected case class SingleSetRequest[A <: Writable, T](val vb:VarBind[A, T]) extends SetRequest[T]
case class CompoundSetRequest[A <: Writable, T, U](val head:VarBind[A, T], val tail:SetRequest[U]) extends SetRequest[(T, U)]