package org.snmp4s

object Mib {
  type Oid = Seq[Int]
  implicit def Int2Oid(i:Int):Oid = Seq(i)
}

import Mib._

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

trait Readable[T] extends MibObject[T]
trait Writable[T] extends MibObject[T] {
  def to(v:T) = SetObj(this, v)
}

sealed trait MaxAccess
trait NoAccess extends MaxAccess
trait ReadOnly[T] extends MaxAccess with Readable[T]
trait ReadWrite[T] extends MaxAccess with Readable[T] with Writable[T]

class MibObjectInst[T](val oid:Oid, val name:String) extends MibObject[T] with Equals {
  def apply(index:Oid) = new MibObjectInst[T](oid ++ index, name+"."+oid.mkString("."))
  
}

case class SetObj[T](val obj:Writable[T], val v:T)

