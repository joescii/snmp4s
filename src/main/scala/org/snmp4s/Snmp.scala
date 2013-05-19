package org.snmp4s

import org.snmp4j.{ Snmp => Snmp4j, CommunityTarget, PDU }
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi.{ GenericAddress, OctetString, VariableBinding, OID, Integer32 }
import org.snmp4j.transport.DefaultUdpTransportMapping

class Snmp(val ip:String, val port:Int, val read:String, val write:String) {
  private val addr = GenericAddress.parse(s"udp:$ip/$port")
  private val map  = new DefaultUdpTransportMapping
  private val snmp = new Snmp4j(map)
  map.listen()
  
  private def target(comm:String) = {
    val target = new CommunityTarget
    target.setCommunity(new OctetString(read))
    target.setAddress(addr)
    target.setRetries(2)
    target.setTimeout(1500)
    target.setVersion(SnmpConstants.version1)
    target
  }
  
  def get[T](obj:Readable[T])(implicit m:Manifest[T]):Either[String,T] = {
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

  def set[T](set:SetObj[T])(implicit m:Manifest[T]):Option[String] = {    
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
}