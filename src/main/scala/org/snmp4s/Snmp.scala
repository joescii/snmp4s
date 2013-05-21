package org.snmp4s

import org.snmp4j.{ Snmp => Snmp4j, CommunityTarget, PDU }
import org.snmp4j.smi.{ GenericAddress, OctetString, VariableBinding, OID, Integer32 }
import org.snmp4j.transport.DefaultUdpTransportMapping

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
  
  def get[T](obj:ReadableDataObject[T])(implicit m:Manifest[T]):Either[String,T] = {
    val pdu = new PDU
    pdu.add(new VariableBinding(new OID(obj.oid.toArray)))
    pdu.setType(PDU.GET)
    
    val event = snmp.get(pdu, target(read))
    val res = event.getResponse
    val vb = res.get(0)
    val v = vb.getVariable
    
    // TODO: Handle other types
    if(m.runtimeClass == classOf[Int]) {
      Right(v.toInt().asInstanceOf[T])
    } else { 
      Right(1.asInstanceOf[T])
    }
  }

  def set[T](set:VarBind[T])(implicit m:Manifest[T]):Option[String] = {    
    if(m.runtimeClass == classOf[Int]){
      val pdu = new PDU
      val vb = new VariableBinding(new OID(set.obj.oid.toArray))
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
  
  def walk[T](obj:ReadOnly[T], ver:Version = Version1)(implicit m:Manifest[T]):Either[String,Seq[VarBind[T]]] = {
    Left("Crap")
  }
}