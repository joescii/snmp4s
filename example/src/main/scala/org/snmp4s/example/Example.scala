package org.snmp4s
package example

import Mib._
import mib._
import IfMib._
import AgentppSimulationMib._
import AgentppSimMode_enum._
import Console.{println => p}

object Example extends App {
  val snmp = new SnmpSync(SnmpParams())

  snmp.get(AgentppSimMode) match {
    case Left(err)   => p("Get failed. "+err)
    case Right(mode) => p(AgentppSimMode(0)+" == "+mode)
  }
  
  snmp.set(AgentppSimMode to Config) match {
    case Some(err) => p("Failed to set "+AgentppSimMode(0))
    case _ => p("Set "+AgentppSimMode(0)+" to config")
  }

  snmp.get(AgentppSimMode) match {
    case Left(err)   => p("Get failed. "+err)
    case Right(mode) => p(AgentppSimMode(0)+" == "+mode)
  }
}