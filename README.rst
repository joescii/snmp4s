**SNMP4S** is an idiomatic, type safe Scala wrapper on top of `SNMP4J`_.  The manifest vision for SNMP4S
is to have an SBT plugin which will allow MIBs to be part of the src directory.  Those MIBs would then
be compiled into Scala objects.  The compiled MIBs along with the base SNMP4S libraries will form a powerful
DSL for manipulating SNMP data.  

Example Code
------------
This can be done today::

  // Instantiate a new Snmp object, shown here with defaults for IP, port, community, etc.
  val snmp = new Snmp(
    ip = "127.0.0.1", 
    port = 161, 
    read = "public", 
    write = "private",
    version = Version1,
    retries = 2,
    timeout = 1500
  )

  // Define the MIB object you want to manipulate (this part will one day be generated from MIBs)
  case object agentppSimMode extends MibObjectInst[Int]
    (Seq(1,3,6,1,4,1,4976,2,1,1,0), "agentppSimMode") with ReadWrite[Int]

  // This get will return Right(1) if there are no errors and the value of the scalar is 1.
  snmp.get(agentppSimMode)

  // This set will return None if everything goes well, or Some error if not
  snmp.set(agentppSimMode to 2)

Futuristic Example Code
-----------------------
This is what I envision.  Note that ``ifIndex``, ``ifType``, ``ethernet_csmacd`` etc were generated from the MIBs::

  val snmp = new Snmp // Plus whatever params you want, including SNMPv3 stuff

  val ethernetAdminStates = (for { 
    varbind <- snmp walk ifIndex
  } yield {
    snmp.get(ifType(varbind.v)) match {
      case ethernet_csmacd => Some((varbind.v, snmp.get(ifAdminStatus(varbind.v))))
	  case _ => None
    }
  }).flatten

If I really get around to doing something awesome, maybe I'll figure out how to minimize the number of messages
transmitted to perform the previous block of code.  In particular, it should perform the ``walk``, perform the ``get``
of all ``ifType`` in one PDU, then perform the ``ifAdminStatus`` gets in one PDU.

I also hope to eventually use `akka`_ to support asynchronous handling of this API.

License
-------

*snmp4s* is licensed under `APL 2.0`_.

Copyright 2013 org.snmp4s

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

.. _SNMP4J: http://www.snmp4j.org/
.. _APL 2.0: http://www.apache.org/licenses/LICENSE-2.0
.. _akka: http://akka.io/
