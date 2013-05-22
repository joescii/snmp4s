**SNMP4S** is an idiomatic, type safe Scala wrapper on top of `SNMP4J`_.  The manifest vision for SNMP4S is to have an SBT plugin which will allow MIBs to be part of the src directory.  Those MIBs would then be compiled into Scala objects.  The compiled MIBs along with the base SNMP4S libraries will form a powerful DSL for manipulating SNMP data.  

Design Goals
------------
1. Provide a powerful DSL that is a pleasure to use.
2. Provide static type checking.
3. Generate code from the MIBs that define the SNMP interface to maximize benefit of type safety.
4. Encapsulate all references to SNMP4J to allow changing out the underlying stack.
5. Provide an event-driven actor-based model for handling requests and responses.

Code That Works
---------------
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

  // Define the MIB objects you want to manipulate (this part will one day be generated from MIBs)
  case object agentppSimMode extends AccessibleObject[ReadWrite, Int]
    (Seq(1,3,6,1,4,1,4976,2,1,1), "agentppSimMode") with Scalar[ReadWrite, Int]
  case object ifDescr extends AccessibleObject[ReadOnly, String]
    (Seq(1,3,6,1,2,1,2,2,1,2), "ifDescr")

  // Get the scalar variable for agentppSimMode.0
  snmp.get(agentppSimMode(0)) match {
    case Left(err) => // Something bad happened
    case Right(v)  => // v is set to the variable's value
  }

  // Set the scalar to 2. Since agentppSimMode is a scalar, we can drop the (0) index.
  snmp.set(agentppSimMode to 2) match {
    case Some(err) => // Something bad happened
    case _         => // It worked
  }

  // Walk ifDescr and return a tuple containing the index and the value
  snmp walk ifDescr match {
    case Left(err)   => Seq() // Something bad happened
    case Right(walk) => walk map { vb => (vb.obj.oid.last, vb.v) }
  }

Code That Doesn't Work
-----------------------
As important as code that works, is code that doesn't.  These mistakes will not compile::

  val snmp = new Snmp

  case object ifDescr extends AccessibleObject[ReadOnly, String]
    (Seq(1,3,6,1,2,1,2,2,1,2), "ifDescr")
  case object ifAdminStatus extends AccessibleObject[ReadWrite, Int]
    (Seq(1,3,6,1,2,1,2,2,1,7), "ifAdminStatus")

  // Cannot set a Read-only OID
  snmp.set(ifDescr(1) to "description")
  // inferred type arguments [org.snmp4s.ReadOnly,String] do not conform to method set's type parameter bounds [A <: org.snmp4s.Writable,T]
  // [error]         snmp.set(ifDescr(1) to "description")
  // [error]              ^

  // Cannot set an OID with an Int syntax with a String
  snmp.set(ifAdminStatus(1) to "2")
  // type mismatch;
  // [error]  found   : String("2")
  // [error]  required: Int
  // [error]           snmp.set(ifAdminStatus(1) to "2")
  // [error]                                        ^

  // Cannot get the wrong type
  val descr:Either[String,Int] = snmp get ifDescr(1)
  // type mismatch;
  // [error]  found   : Either[String,String]
  // [error]  required: Either[String,Int]
  // [error]           val descr:Either[String,Int] = snmp get ifDescr(1)
  // [error]                                               ^


Futuristic Example Code
-----------------------
This is what I envision.  Note that ``ifIndex``, ``ifType``, ``ethernet_csmacd`` etc were generated from the MIBs::

  val snmp = new Snmp // Instantiated with whatever params you want, including SNMPv3 stuff

  val ethernetAdminStates = (for { 
    varbind <- snmp walk ifIndex
  } yield {
    snmp.get(ifType(varbind.v)) match {
      case ethernet_csmacd => Some((varbind.v, snmp.get(ifAdminStatus(varbind.v))))
	  case _ => None
    }
  }).flatten

  // Can get multiple variables and they're all the correct type
  val Either[String,(Int, String, Int)] = snmp.get(ifIndex(1), ifDescr(1), ifAdminStatus(1))


If I really get around to doing something awesome, maybe I'll figure out how to minimize the number of messages
transmitted to perform the previous block of code.  In particular, it should perform the ``walk``, perform the ``get``
of all ``ifType`` in one PDU, then perform the ``ifAdminStatus`` gets in one PDU.

I also hope to eventually use `akka`_ to support asynchronous handling of this API.

Environment
-----------
Other than the usual need for git, sbt, and jdk, for this project sbt will need root access to run the test suites.  The integration tests use SNMP4J-Agent which needs to bind to port 161.  

Contributions
-------------
Pull requests are welcomed.

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
