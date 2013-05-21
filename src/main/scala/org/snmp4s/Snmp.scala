package org.snmp4s

import org.snmp4j.{ Snmp => Snmp4j, CommunityTarget, PDU }
import org.snmp4j.smi.{ GenericAddress, OctetString, VariableBinding, OID, Integer32, Variable }
import org.snmp4j.transport.DefaultUdpTransportMapping
import org.snmp4j.util.TreeUtils
import org.snmp4j.util.DefaultPDUFactory
import scala.collection.JavaConversions._
import Mib._

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
  
  def get[A <: Readable, T](obj:DataObject[A, T])(implicit m:Manifest[T]):Either[String,T] = {
    val pdu = new PDU
    pdu.add(new VariableBinding(obj.oid))
    pdu.setType(PDU.GET)
    
    val event = snmp.get(pdu, target(read))
    val res = event.getResponse
    val vb = res.get(0)
    val v = vb.getVariable

    Right(cast(v))
  }

  def set[A <: Writable, T](set:VarBind[A, T])(implicit m:Manifest[T]):Option[String] = {    
    if(m.runtimeClass == classOf[Int]){
      val pdu = new PDU
      val vb = new VariableBinding(set.obj.oid)
      vb.setVariable(new Integer32(set.v.asInstanceOf[Int]))
      pdu.add(vb)
      pdu.setType(PDU.SET)

      val event = snmp.set(pdu, target(write))
      
      None
    }
    else {
      Some("Unsupported syntax")
    }
  }
  
  def walk[A <: Readable, T](obj:AccessibleObject[A, T], ver:Version = Version1)(implicit m:Manifest[T]):Either[String,Seq[VarBind[A, T]]] = {
    val events = (new TreeUtils(snmp, new DefaultPDUFactory(PDU.GETNEXT))).walk(target(read), Array(obj.oid))
    val vbs:Seq[VarBind[A, T]] = for {
      event <- events
      vb <- event.getVariableBindings()
    } yield {
      val o:Oid = vb.getOid()
      val v = vb.getVariable
      VarBind(obj(o.last), cast(v))
    }
    
    Right(vbs)
  }
  
  // TODO: Handle other types
  private def cast[T](v:Variable)(implicit m:Manifest[T]):T = {
    val c = m.runtimeClass
    if (c == classOf[Int]) 
      v.toInt().asInstanceOf[T]
    else if(c == classOf[String])
      v.toString().asInstanceOf[T]
    else
      1.asInstanceOf[T]
    
  }
}