**SNMP4S** is an idiomatic type-safe Scala DSL for SNMP.  This library begins with an SBT plugin which treats MIBs as source files.  These MIBs are parsed and compiled into Scala objects.  The compiled MIBs along with the base **SNMP4S** libraries form a powerful DSL for manipulating SNMP data.  

Design Goals
------------
* Provide a powerful DSL that is a pleasure to use.
* Reduce programmer mistakes by utilizing Scala's type system.
* Generate code from the MIBs that define the SNMP interface to maximize the benefit of type safety.
* Encapsulate all references to SNMP4J to allow changing out the underlying stack.
* Provide multiple programming models for handling requests, responses, and errors (synchronous, asynchronous, excepting, etc)

Components
----------
**SNMP4S** consists of three components

1. snmp4s-core: The wrapper around `SNMP4J`_ that does the runtime work (licensed under `APL 2.0`_.)
2. snmp4s-gen:  The code that utilizes `Mibble`_ to generate Scala code (licensed under `GPL 3.0`_.)
3. snmp4s-sbt:  The SBT plugin that utilizes snmp4s-gen to compile MIBs (licesned under `GPL 3.0`_.)

Installation
------------
The latest release is **0.1.0**.  *snmp4s-core* is built against 2.10.1, but *snmp4s-gen* and *snmp4s-sbt* are built against 2.9.2 for use in SBT 0.12.x.

First, you need to add *snmp4s-sbt* as an SBT `Plugin`_ in your project.  Although **SNMP4S** is available from Maven Central, our dependency on `Mibble`_ will require a resolver in addition to the plugin declaration

.. code:: scala

  resolvers += "Mibble" at "http://maven.cloudhopper.com/repos/third-party/"

  addSbtPlugin("org.snmp4s" % "snmp4s-sbt" % "0.1.0")

Secondly, you need to add *snmp4s-core* as a dependency in your project build file.  Again, we require an additional resolver but this time for our dependency on `SNMP4J`_

.. code:: scala

  resolvers += "OO SNMP" at "https://oosnmp.net/dist/release/"

  libraryDependencies += "org.snmp4s" %% "snmp4s-core" % "0.1.0"

Configuration
-------------

Any MIBs that are built in as part of **SNMP4S** can be declared in your project build file (see the ``org.snmp4s.gen.BuiltIn`` scaladocs/source for an exhaustive list of available MIBs)

.. code:: scala

  import org.snmp4s.gen.BuiltIn._

  snmp4sBuiltInMibs := Seq(IfMib, AdslLineMib)

Custom MIBs are simply placed in the ``src/main/mibs`` directory of your project.  All files in the directory will be treated as a MIB regardless of filename

.. code:: scala

  ~/code/snmp4s/example/src/main/mibs $ ls -1
  AGENTPP-GLOBAL-REG.txt
  AGENTPP-SIMULATION-MIB.txt

Finally, you can specify the package prefix for all of the generated MIB objects, shown here with the default value::

  snmp4sMibPackage := "org.snmp4s.mib"

Each MIB file will produce a Scala file in package <snmp4sMibPackage>.<CamelCasedMibName>.  For instance, the IF-MIB with the default ``snmp4sMibPackage`` value will create the package ``org.snmp4s.mib.IfMib``.  A case class is generated for every leaf OID in the MIB.  Also, enumeration classes are generated for OIDs with an enumerated integer syntax.

Code Examples That Work
---------------
This can be done today.  See the *example* directory for a working project which utilizes **SNMP4S**

.. code:: scala

  // Instantiate a new Snmp object, shown here with defaults for IP, port, community, etc.
  val snmp = new Snmp(SnmpParams(
    ip = "127.0.0.1", 
    port = 161, 
    read = "public", 
    write = "private",
    version = Version1,
    retries = 2,
    timeout = 1500
  ))

  // Import some implicits
  import Mib._

  // Import the compiled MIB packages.  This package is settable via snmp4sMibPackage property
  import org.snmp4s.mib._

  // Import the compiled MIBs you want to work with
  import IfMib._
  
  // Get the value assigned to ifDescr.1
  snmp.get(IfDescr(1)) match {
    case Left(err) => // Something bad happened
    case Right(v)  => // v is set to the ifDescr.1's value
  }

  // Get ifNumber.0. Since it is a scalar, we can drop the (0) index.
  snmp.get(IfNumber) match {
    case Left(err) => // Something bad happened
    case Right(v)  => // v is set to the ifNumber's value
  }

  // Set ifAlias.1 to "My Interface"
  snmp.set(IfAlias(1) to "My Interface") match {
    case Some(err) => // Something bad happened
    case _         => // It worked
  }

  // Walk ifDescr and return tuples containing the index and the value
  snmp walk IfDescr match {
    case Left(err)   => Seq() // Something bad happened
    case Right(walk) => walk map { vb => (vb.obj.oid.last, vb.v) }
  }

  // OIDs with enumerated integer syntax are a cinch to work with
  import IfAdminStatus_enum._
  set(IfAdminStatus(1) to Down)
  get(IfAdminStatus(1)) match {
    case Left(err) =>  // Something bad happened
    case Right(status) => status match {
      case Up      =>  // I'm up
      case Down    =>  // I'm down
      case Testing =>  // I'm testing
    }
  }

  // Can pattern match against the OIDs
  val testPorts = snmp walk IfAdminStatus match {
    case Left(err)   => Seq() // Something bad happened
    case Right(walk) => for(VarBind(IfAdminStatus(Seq(i)), Testing) <- walk) yield i
  }

Code Examples That Don't Work
-----------------------
As important as code that works, is code that doesn't.  These mistakes will not compile

.. code:: scala

  val snmp = new Snmp(SnmpParams())

  // Cannot set a Read-only OID
  snmp.set(IfDescr(1) to "description")
  // inferred type arguments [org.snmp4s.ReadOnly,String] do not conform to method set's type 
  // parameter bounds [A <: org.snmp4s.Writable,T]
  // [error]         snmp.set(IfDescr(1) to "description")
  // [error]              ^

  // Cannot set an OID with an Int syntax with a String
  snmp.set(IfAdminStatus(1) to "2")
  // type mismatch;
  // [error]  found   : String("2")
  // [error]  required: Int
  // [error]           snmp.set(IfAdminStatus(1) to "2")
  // [error]                                        ^

  // Cannot get the wrong type
  val descr:Either[String,Int] = snmp get IfDescr(1)
  // type mismatch;
  // [error]  found   : Either[String,String]
  // [error]  required: Either[String,Int]
  // [error]           val descr:Either[String,Int] = snmp get IfDescr(1)
  // [error]                                               ^

Environment
-----------
Other than the usual need for git, sbt, and jdk, sbt will need root access to run the test suites for the *snmp4s-core* project.  The integration tests use SNMP4J-Agent which needs to bind to port 161.  

Contributions
-------------
Pull requests are welcomed.

License
-------

*snmp4s-core* is licensed under `APL 2.0`_.

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

*snmp4s-gen* and *snmp4s-sbt* are licensed under `GPL 3.0`_.

While I prefer APL, *snmp4s-gen* and hence *snmp4s-sbt* utilize `Mibble`_ which is a GPL-licensed library.  Fortunately for any use cases I have imagined, you will only use *snmp4s-gen* and *snmp4s-sbt* in development.  Of the three projects, only *snmp4s-core* needs to be utilized by your running application.

.. _SNMP4J: http://www.snmp4j.org/
.. _APL 2.0: http://www.apache.org/licenses/LICENSE-2.0
.. _GPL 3.0: http://www.gnu.org/licenses/gpl.html
.. _Mibble: http://www.mibble.org/index.html
.. _Plugin: http://www.scala-sbt.org/release/docs/Getting-Started/Using-Plugins
