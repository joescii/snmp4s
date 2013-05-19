**snmp4s** is an idiomatic, type safe Scala wrapper on top of Snmp4j.  The manifest vision for Snmp4s
is that an SBT project will have MIBs as a source, which get compiled into Scala
objects.  The compiled MIBs along with the base Snmp4s libraries will form a powerful
DSL for manipulating SNMP data.  Example code::

  // Instantiate a new ``Snmp`` object with defaults for IP, port, community, etc.
  val snmp = new Snmp

  // Define the MIB object you want to manipulate (this part will one day be generated from MIBs)
  case object agentppSimMode extends MibObjectInst[Int](Seq(1,3,6,1,4,1,4976,2,1,1,0), "agentppSimMode") with ReadWrite[Int]

  // This get will return ``Right(1)`` if there are no errors and the value of the scalar is ``1``.
  snmp.get(agentppSimMode)

  // This set will return ``None`` if everything goes well, or ``Some`` error if not
  snmp.set(agentppSimMode to 2)

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

.. _APL 2.0: http://www.apache.org/licenses/LICENSE-2.0
