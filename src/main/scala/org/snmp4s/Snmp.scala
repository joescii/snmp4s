package org.snmp4s

import org.snmp4j.{ Snmp => Snmp4j, CommunityTarget, PDU }
import org.snmp4j.smi.{ GenericAddress, OctetString, VariableBinding, OID, Integer32, Variable }
import org.snmp4j.transport.DefaultUdpTransportMapping
import org.snmp4j.util.TreeUtils
import org.snmp4j.util.DefaultPDUFactory
import scala.collection.JavaConversions._
import Mib._

sealed trait SnmpError
case object AuthorizationError	extends SnmpError
case object BadValue			extends SnmpError
case object CommitFailed		extends SnmpError
case object GeneralError		extends SnmpError
case object InconsistentName	extends SnmpError
case object InconsistentValue	extends SnmpError
case object NoAccessError		extends SnmpError
case object NoCreation			extends SnmpError
case object NoSuchName			extends SnmpError
case object NotWritable			extends SnmpError
case object ReadOnlyError		extends SnmpError
case object ResourceUnavailable	extends SnmpError	
case object TooBig				extends SnmpError
case object UndoFailed			extends SnmpError
case object WrongEncoding       extends SnmpError
case object WrongLength			extends SnmpError
case object WrongType			extends SnmpError
case object WrongValue			extends SnmpError
case object UnsupportedSyntax   extends SnmpError
case object AgentUnreachable	extends SnmpError
case class ExceptionThrown(val e:Exception) extends SnmpError
case class UndefinedError(val i:Int) extends SnmpError

private object ErrorMap extends (Int => SnmpError) {
  import org.snmp4j.mp.SnmpConstants._
  
  private val m = Map(
    SNMP_ERROR_AUTHORIZATION_ERROR -> AuthorizationError,	
	SNMP_ERROR_BAD_VALUE -> BadValue,
	SNMP_ERROR_COMMIT_FAILED -> CommitFailed,
	SNMP_ERROR_GENERAL_ERROR -> GeneralError,
	SNMP_ERROR_INCONSISTENT_NAME -> InconsistentName,
	SNMP_ERROR_INCONSISTENT_VALUE -> InconsistentValue,
	SNMP_ERROR_NO_ACCESS -> NoAccessError,
	SNMP_ERROR_NO_CREATION -> NoCreation,
	SNMP_ERROR_NO_SUCH_NAME -> NoSuchName,
	SNMP_ERROR_NOT_WRITEABLE -> NotWritable,
	SNMP_ERROR_READ_ONLY -> ReadOnlyError,
	SNMP_ERROR_RESOURCE_UNAVAILABLE -> ResourceUnavailable,
	SNMP_ERROR_TOO_BIG -> TooBig,
	SNMP_ERROR_UNDO_FAILED -> UndoFailed,
	SNMP_ERROR_WRONG_ENCODING -> WrongEncoding,
	SNMP_ERROR_WRONG_LENGTH -> WrongLength,
	SNMP_ERROR_WRONG_TYPE -> WrongType,
	SNMP_ERROR_WRONG_VALUE -> WrongValue
  )
  
  def apply(i:Int) = m.get(i) match {
    case Some(e) => e
    case _ => UndefinedError(i)
  }
}

/**
  * Create one of these to do SNMP.
  */
class Snmp(
    val ip:String = "127.0.0.1", 
    val port:Int = 161, 
    val read:String = "public", 
    val write:String = "private",
    val version:Version = Version1,
    val retries:Int = 2,
    val timeout:Long = 1500) {
  
  private val addr = GenericAddress.parse(s"udp:$ip/$port")
  private val map  = new DefaultUdpTransportMapping
  private val snmp = new Snmp4j(map)
  map.listen()
    
  private def target(comm:String) = {
    val target = new CommunityTarget
    target.setCommunity(new OctetString(read))
    target.setAddress(addr)
    target.setRetries(retries)
    target.setTimeout(timeout)
    target.setVersion(version.enum)
    target
  }
  
  implicit def Oid2Snmp4j(o:Oid):OID = new OID(o.toArray)
  implicit def Snmp4j2Oid(o:OID):Oid = o.getValue()
  
  def get[A <: Readable, T](obj:DataObject[A, T])(implicit m:Manifest[T]):Either[SnmpError,T] = {
    try {
      val pdu = new PDU
      pdu.add(new VariableBinding(obj.oid))
      pdu.setType(PDU.GET)

      val event = snmp.get(pdu, target(read))
      val res = Option(event.getResponse)

      res match {
        case Some(res) => if (res.getErrorStatus > 0) {
          val i = res.getErrorIndex()
          val vb = res.get(i - 1)
          val v = vb.getVariable
          Left(ErrorMap(res.getErrorStatus))
        } else {
          val vb = res.get(0)
          val v = vb.getVariable
          Right(cast(obj, v))
        }
        case None => {
          Left(AgentUnreachable)
        }
      }
    } catch {
      case e:NullPointerException => Left(AgentUnreachable)
    }
  }

  def set[A <: Writable, T](set:VarBind[A, T])(implicit m:Manifest[T]):Option[SnmpError] = {    
    toVariable(set.obj, set.v) match {
      case Some(v) => {
        try {
          val pdu = new PDU
          val vb = new VariableBinding(set.obj.oid)
          vb.setVariable(v)
          pdu.add(vb)
          pdu.setType(PDU.SET)

          val event = snmp.set(pdu, target(write))
          val res = Option(event.getResponse)

          res match {
            case Some(res) => if (res.getErrorStatus > 0) {
              val i = res.getErrorIndex()
              val vb = res.get(i - 1)
              val v = vb.getVariable
              Some(ErrorMap(res.getErrorStatus))
            } else None
            case None => Some(AgentUnreachable)
          }
        } catch {
          case e: NullPointerException => Some(AgentUnreachable)
        }
      }
      case _ => Some(UnsupportedSyntax)
    }
  }
  
  private def toVariable[A <: Writable, T](obj:MibObject[A], v:T)(implicit m:Manifest[T]):Option[Variable] = { 
    val c = m.runtimeClass
    if(c == classOf[Int]) Some(new Integer32(v.asInstanceOf[Int]))
    else if(c == classOf[String]) Some(new OctetString(v.asInstanceOf[String]))
    else if(obj.enum.isDefined) Some(new Integer32(fromEnum(v)))
    else None
  }
  
  private def fromEnum[T](v:T):Int = {
    val c = v.getClass()
    val m = c.getDeclaredMethod("id")
    val r = m.invoke(v)
    r.asInstanceOf[Int]
  }
  
  def walk[A <: Readable, T](obj:AccessibleObject[A, T], ver:Version = Version1)(implicit m:Manifest[T]):Either[SnmpError,Seq[VarBind[A, T]]] = {
    try {
      val events = (new TreeUtils(snmp, new DefaultPDUFactory(PDU.GETNEXT))).walk(target(read), Array(obj.oid))
      val vbs: Seq[VarBind[A, T]] = for {
        event <- events
        vb <- event.getVariableBindings()
      } yield {
        val o: Oid = vb.getOid()
        val v = vb.getVariable
        VarBind(obj(o.last), cast(obj, v))
      }

      Right(vbs)
    } catch {
      // This is a little sketchy.  I know for sure that a NullPointer is thrown by snmp4j when
      // the agent is unreachable.  However, there could be other conditions that cause a 
      // NullPointerException.
      case n:NullPointerException => Left(AgentUnreachable)
    }
  }
  
  // TODO: Handle other types
  private def cast[A <: Readable, T](obj:MibObject[A], v:Variable)(implicit m:Manifest[T]):T = {
    val c = m.runtimeClass
    val r = if (c == classOf[Int]) 
      v.toInt()
    else if(c == classOf[String])
      v.toString()
    else if(obj.enum isDefined)
      obj.enum.get(v.toInt)
    else
      1
    
    r.asInstanceOf[T]
  }
}