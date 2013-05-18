package org.snmp4s

class Snmp {
  def get[T: Manifest](obj:Readable[T]):Either[String,T] = {
    Right(1.asInstanceOf[T])
  }

  def set[T: Manifest](obj:Writable[T], v:T):Option[String] = {
    None
  }
}