package org.snmp4s;

import java.io.*;

import org.apache.log4j.*;
import org.snmp4j.*;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.mp.*;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.*;
import org.snmp4j.agent.io.ImportModes;
import org.snmp4j.util.ThreadPool;
import org.snmp4j.log.Log4jLogFactory;
import org.snmp4j.log.LogFactory;
import org.snmp4j.agent.mo.snmp4j.example.Snmp4jHeartbeatMib;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.agent.mo.ext.AgentppSimulationMib;

/**
 * The <code>TestAgent</code> is a sample SNMP agent implementation of all
 * features (MIB implementations) provided by the SNMP4J-Agent framework.
 * The <code>TestAgent</code> extends the <code>BaseAgent</code> which provides
 * a framework for custom agent implementations through hook methods. Those
 * abstract hook methods need to be implemented by extending the
 * <code>BaseAgent</code>.
 * <p>
 * This IF-MIB implementation part of this test agent, is instrumentation as
 * a simulation MIB. Thus, by changing the agentppSimMode
 * (1.3.6.1.4.1.4976.2.1.1.0) from 'oper(1)' to 'config(2)' any object of the
 * IF-MIB is writable and even creatable (columnar objects) via SNMP. Check it
 * out!
 * <p>
 * Note, for snmp4s, this code is mostly a copy from snmp4j.
 *
 * @author Frank Fock, barnesjd
 * @version 1.0
 */
public class TestAgent extends BaseAgent {

  // initialize Log4J logging
  static {
    LogFactory.setLogFactory(new Log4jLogFactory());
  }

  protected String address;
  private Snmp4jHeartbeatMib heartbeatMIB;
  private AgentppSimulationMib agentppSimulationMIB;

  /**
   * Creates the test agent with a file to read and store the boot counter and
   * a file to read and store its configuration.
   *
   * @param bootCounterFile
   *    a file containing the boot counter in serialized form (as expected by
   *    BaseAgent).
   * @param configFile
   *    a configuration file with serialized management information.
   * @throws IOException
   *    if the boot counter or config file cannot be read properly.
   */
  public TestAgent(File bootCounterFile, File configFile) throws IOException {
    super(bootCounterFile, configFile,
          new CommandProcessor(new OctetString(MPv3.createLocalEngineID())));
// Alternatively:       OctetString.fromHexString("00:00:00:00:00:00:02", ':');
    agent.setWorkerPool(ThreadPool.create("RequestPool", 4));
  }

  protected void registerManagedObjects() {
    try {
      server.register(createStaticIfTable(), null);
      server.register(createStaticIfXTable(), null);
      agentppSimulationMIB.registerMOs(server, null);
      heartbeatMIB.registerMOs(server, null);
    }
    catch (DuplicateRegistrationException ex) {
      ex.printStackTrace();
    }
  }

  protected void addNotificationTargets(SnmpTargetMIB targetMIB,
                                        SnmpNotificationMIB notificationMIB) {
    targetMIB.addDefaultTDomains();

    targetMIB.addTargetAddress(new OctetString("notificationV2c"),
                               TransportDomains.transportDomainUdpIpv4,
                               new OctetString(new UdpAddress("127.0.0.1/162").getValue()),
                               200, 1,
                               new OctetString("notify"),
                               new OctetString("v2c"),
                               StorageType.permanent);
    targetMIB.addTargetAddress(new OctetString("notificationV3"),
                               TransportDomains.transportDomainUdpIpv4,
                               new OctetString(new UdpAddress("127.0.0.1/1162").getValue()),
                               200, 1,
                               new OctetString("notify"),
                               new OctetString("v3notify"),
                               StorageType.permanent);
    targetMIB.addTargetParams(new OctetString("v2c"),
                              MessageProcessingModel.MPv2c,
                              SecurityModel.SECURITY_MODEL_SNMPv2c,
                              new OctetString("cpublic"),
                              SecurityLevel.AUTH_PRIV,
                              StorageType.permanent);
    targetMIB.addTargetParams(new OctetString("v3notify"),
                              MessageProcessingModel.MPv3,
                              SecurityModel.SECURITY_MODEL_USM,
                              new OctetString("v3notify"),
                              SecurityLevel.NOAUTH_NOPRIV,
                              StorageType.permanent);
    notificationMIB.addNotifyEntry(new OctetString("default"),
                                   new OctetString("notify"),
                                   SnmpNotificationMIB.SnmpNotifyTypeEnum.inform,
                                   StorageType.permanent);
  }

  protected void addViews(VacmMIB vacm) {
    vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv1,
                  new OctetString("cpublic"),
                  new OctetString("v1v2group"),
                  StorageType.nonVolatile);
    vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c,
                  new OctetString("cpublic"),
                  new OctetString("v1v2group"),
                  StorageType.nonVolatile);
    vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                  new OctetString("SHADES"),
                  new OctetString("v3group"),
                  StorageType.nonVolatile);
    vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                  new OctetString("MD5DES"),
                  new OctetString("v3group"),
                  StorageType.nonVolatile);
    vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                  new OctetString("TEST"),
                  new OctetString("v3test"),
                  StorageType.nonVolatile);
    vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                  new OctetString("SHA"),
                  new OctetString("v3restricted"),
                  StorageType.nonVolatile);
    vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                  new OctetString("SHAAES128"),
                  new OctetString("v3group"),
                  StorageType.nonVolatile);
    vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                  new OctetString("SHAAES192"),
                  new OctetString("v3group"),
                  StorageType.nonVolatile);
    vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                  new OctetString("SHAAES256"),
                  new OctetString("v3group"),
                  StorageType.nonVolatile);
    vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                  new OctetString("MD5AES128"),
                  new OctetString("v3group"),
                  StorageType.nonVolatile);
    vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                  new OctetString("MD5AES192"),
                  new OctetString("v3group"),
                  StorageType.nonVolatile);
    vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                  new OctetString("MD5AES256"),
                  new OctetString("v3group"),
                  StorageType.nonVolatile);
    vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                  new OctetString("v3notify"),
                  new OctetString("v3group"),
                  StorageType.nonVolatile);

    vacm.addAccess(new OctetString("v1v2group"), new OctetString("public"),
                   SecurityModel.SECURITY_MODEL_ANY,
                   SecurityLevel.NOAUTH_NOPRIV,
                   MutableVACM.VACM_MATCH_EXACT,
                   new OctetString("fullReadView"),
                   new OctetString("fullWriteView"),
                   new OctetString("fullNotifyView"),
                   StorageType.nonVolatile);
    vacm.addAccess(new OctetString("v3group"), new OctetString(),
                   SecurityModel.SECURITY_MODEL_USM,
                   SecurityLevel.AUTH_PRIV,
                   MutableVACM.VACM_MATCH_EXACT,
                   new OctetString("fullReadView"),
                   new OctetString("fullWriteView"),
                   new OctetString("fullNotifyView"),
                   StorageType.nonVolatile);
    vacm.addAccess(new OctetString("v3restricted"), new OctetString(),
                   SecurityModel.SECURITY_MODEL_USM,
                   SecurityLevel.NOAUTH_NOPRIV,
                   MutableVACM.VACM_MATCH_EXACT,
                   new OctetString("restrictedReadView"),
                   new OctetString("restrictedWriteView"),
                   new OctetString("restrictedNotifyView"),
                   StorageType.nonVolatile);
    vacm.addAccess(new OctetString("v3test"), new OctetString(),
                   SecurityModel.SECURITY_MODEL_USM,
                   SecurityLevel.AUTH_PRIV,
                   MutableVACM.VACM_MATCH_EXACT,
                   new OctetString("testReadView"),
                   new OctetString("testWriteView"),
                   new OctetString("testNotifyView"),
                   StorageType.nonVolatile);

    vacm.addViewTreeFamily(new OctetString("fullReadView"), new OID("1.3"),
                           new OctetString(), VacmMIB.vacmViewIncluded,
                           StorageType.nonVolatile);
    vacm.addViewTreeFamily(new OctetString("fullWriteView"), new OID("1.3"),
                           new OctetString(), VacmMIB.vacmViewIncluded,
                           StorageType.nonVolatile);
    vacm.addViewTreeFamily(new OctetString("fullNotifyView"), new OID("1.3"),
                           new OctetString(), VacmMIB.vacmViewIncluded,
                           StorageType.nonVolatile);

    vacm.addViewTreeFamily(new OctetString("restrictedReadView"),
                           new OID("1.3.6.1.2"),
                           new OctetString(), VacmMIB.vacmViewIncluded,
                           StorageType.nonVolatile);
    vacm.addViewTreeFamily(new OctetString("restrictedWriteView"),
                           new OID("1.3.6.1.2.1"),
                           new OctetString(),
                           VacmMIB.vacmViewIncluded,
                           StorageType.nonVolatile);
    vacm.addViewTreeFamily(new OctetString("restrictedNotifyView"),
                           new OID("1.3.6.1.2"),
                           new OctetString(), VacmMIB.vacmViewIncluded,
                           StorageType.nonVolatile);
    vacm.addViewTreeFamily(new OctetString("restrictedNotifyView"),
                           new OID("1.3.6.1.6.3.1"),
                           new OctetString(), VacmMIB.vacmViewIncluded,
                           StorageType.nonVolatile);

    vacm.addViewTreeFamily(new OctetString("testReadView"),
                           new OID("1.3.6.1.2"),
                           new OctetString(), VacmMIB.vacmViewIncluded,
                           StorageType.nonVolatile);
    vacm.addViewTreeFamily(new OctetString("testReadView"),
                           new OID("1.3.6.1.2.1.1"),
                           new OctetString(), VacmMIB.vacmViewExcluded,
                           StorageType.nonVolatile);
    vacm.addViewTreeFamily(new OctetString("testWriteView"),
                           new OID("1.3.6.1.2.1"),
                           new OctetString(),
                           VacmMIB.vacmViewIncluded,
                           StorageType.nonVolatile);
    vacm.addViewTreeFamily(new OctetString("testNotifyView"),
                           new OID("1.3.6.1.2"),
                           new OctetString(), VacmMIB.vacmViewIncluded,
                           StorageType.nonVolatile);

  }

  protected void addUsmUser(USM usm) {
    UsmUser user = new UsmUser(new OctetString("SHADES"),
                               AuthSHA.ID,
                               new OctetString("SHADESAuthPassword"),
                               PrivDES.ID,
                               new OctetString("SHADESPrivPassword"));
//    usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
    usm.addUser(user.getSecurityName(), null, user);
    user = new UsmUser(new OctetString("TEST"),
                               AuthSHA.ID,
                               new OctetString("maplesyrup"),
                               PrivDES.ID,
                               new OctetString("maplesyrup"));
    usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
    user = new UsmUser(new OctetString("SHA"),
                               AuthSHA.ID,
                               new OctetString("SHAAuthPassword"),
                               null,
                               null);
    usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
    user = new UsmUser(new OctetString("SHADES"),
                               AuthSHA.ID,
                               new OctetString("SHADESAuthPassword"),
                               PrivDES.ID,
                               new OctetString("SHADESPrivPassword"));
    usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
    user = new UsmUser(new OctetString("MD5DES"),
                               AuthMD5.ID,
                               new OctetString("MD5DESAuthPassword"),
                               PrivDES.ID,
                               new OctetString("MD5DESPrivPassword"));
    usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
    user = new UsmUser(new OctetString("SHAAES128"),
                               AuthSHA.ID,
                               new OctetString("SHAAES128AuthPassword"),
                               PrivAES128.ID,
                               new OctetString("SHAAES128PrivPassword"));
    usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
    user = new UsmUser(new OctetString("SHAAES192"),
                               AuthSHA.ID,
                               new OctetString("SHAAES192AuthPassword"),
                               PrivAES192.ID,
                               new OctetString("SHAAES192PrivPassword"));
    usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
    user = new UsmUser(new OctetString("SHAAES256"),
                               AuthSHA.ID,
                               new OctetString("SHAAES256AuthPassword"),
                               PrivAES256.ID,
                               new OctetString("SHAAES256PrivPassword"));
    usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);

    user = new UsmUser(new OctetString("MD5AES128"),
                               AuthMD5.ID,
                               new OctetString("MD5AES128AuthPassword"),
                               PrivAES128.ID,
                               new OctetString("MD5AES128PrivPassword"));
    usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
    user = new UsmUser(new OctetString("MD5AES192"),
                               AuthMD5.ID,
                               new OctetString("MD5AES192AuthPassword"),
                               PrivAES192.ID,
                               new OctetString("MD5AES192PrivPassword"));
    usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
    user = new UsmUser(new OctetString("MD5AES256"),
                               AuthMD5.ID,
                               new OctetString("MD5AES256AuthPassword"),
                               PrivAES256.ID,
                               new OctetString("MD5AES256PrivPassword"));
    usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
    user = new UsmUser(new OctetString("MD5AES256"),
                               AuthMD5.ID,
                               new OctetString("MD5AES256AuthPassword"),
                               PrivAES256.ID,
                               new OctetString("MD5AES256PrivPassword"));
    usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
    user = new UsmUser(new OctetString("v3notify"),
                               null,
                               null,
                               null,
                               null);
    usm.addUser(user.getSecurityName(), null, user);
  }

/* This code illustrates how a table can be created and filled with static
data: */

  private static DefaultMOTable createStaticIfXTable() {
    MOTableSubIndex[] subIndexes =
        new MOTableSubIndex[] { new MOTableSubIndex(SMIConstants.SYNTAX_INTEGER) };
    MOTableIndex indexDef = new MOTableIndex(subIndexes, false);
    MOColumn[] columns = new MOColumn[19];
    int c = 0;
    columns[c++] =
        new MOColumn(c, SMIConstants.SYNTAX_OCTET_STRING,
                     MOAccessImpl.ACCESS_READ_ONLY);     // ifName
    columns[c++] =
        new MOColumn(c, SMIConstants.SYNTAX_COUNTER32,
                     MOAccessImpl.ACCESS_READ_ONLY);// ifInMulticastPkts
    columns[c++] =
            new MOColumn(c, SMIConstants.SYNTAX_COUNTER32,
                         MOAccessImpl.ACCESS_READ_ONLY);// ifInBroadcastPkts
    columns[c++] =
            new MOColumn(c, SMIConstants.SYNTAX_COUNTER32,
                         MOAccessImpl.ACCESS_READ_ONLY);// ifOutMulticastPkts
    columns[c++] =
            new MOColumn(c, SMIConstants.SYNTAX_COUNTER32,
                         MOAccessImpl.ACCESS_READ_ONLY);// ifOutBroadcastPkts
    columns[c++] =
            new MOColumn(c, SMIConstants.SYNTAX_COUNTER32,
                         MOAccessImpl.ACCESS_READ_ONLY);// ifHCInOctets
    columns[c++] =
            new MOColumn(c, SMIConstants.SYNTAX_COUNTER32,
                         MOAccessImpl.ACCESS_READ_ONLY);// ifHCInUcastPkts
    columns[c++] =
            new MOColumn(c, SMIConstants.SYNTAX_COUNTER32,
                         MOAccessImpl.ACCESS_READ_ONLY);// ifHCInMulticastPkts
    columns[c++] =
            new MOColumn(c, SMIConstants.SYNTAX_COUNTER32,
                         MOAccessImpl.ACCESS_READ_ONLY);// ifHCInBroadcastPkts
    columns[c++] =
            new MOColumn(c, SMIConstants.SYNTAX_COUNTER32,
                         MOAccessImpl.ACCESS_READ_ONLY);// ifHCOutOctets
    columns[c++] =
            new MOColumn(c, SMIConstants.SYNTAX_COUNTER32,
                         MOAccessImpl.ACCESS_READ_ONLY);// ifHCOutUcastPkts
    columns[c++] =
            new MOColumn(c, SMIConstants.SYNTAX_COUNTER32,
                         MOAccessImpl.ACCESS_READ_ONLY);// ifHCOutMulticastPkts
    columns[c++] =
            new MOColumn(c, SMIConstants.SYNTAX_COUNTER32,
                         MOAccessImpl.ACCESS_READ_ONLY);// ifHCOutBroadcastPkts
    columns[c++] =
        new MOColumn(c, SMIConstants.SYNTAX_INTEGER,
                     MOAccessImpl.ACCESS_READ_WRITE);     // ifLinkUpDownTrapEnable
    columns[c++] =
        new MOColumn(c, SMIConstants.SYNTAX_GAUGE32,
                     MOAccessImpl.ACCESS_READ_ONLY);     // ifHighSpeed
    columns[c++] =
        new MOColumn(c, SMIConstants.SYNTAX_INTEGER,
                     MOAccessImpl.ACCESS_READ_WRITE);     // ifPromiscuousMode
    columns[c++] =
        new MOColumn(c, SMIConstants.SYNTAX_INTEGER,
                     MOAccessImpl.ACCESS_READ_ONLY);// ifConnectorPresent
    columns[c++] =
        new MOMutableColumn(c, SMIConstants.SYNTAX_OCTET_STRING,     // ifAlias
                            MOAccessImpl.ACCESS_READ_WRITE, null);
    columns[c++] =
        new MOColumn(c, SMIConstants.SYNTAX_TIMETICKS,
                     MOAccessImpl.ACCESS_READ_ONLY);     // ifCounterDiscontinuityTime

    DefaultMOTable ifXTable =
        new DefaultMOTable(new OID("1.3.6.1.2.1.31.1.1"), indexDef, columns);
    MOMutableTableModel model = (MOMutableTableModel) ifXTable.getModel();
    Variable[] rowValues1 = new Variable[] {
        new OctetString("Ethernet-0"),
        new Integer32(1),
        new Integer32(2),
        new Integer32(3),
        new Integer32(4),
        new Integer32(5),
        new Integer32(6),
        new Integer32(7),
        new Integer32(8),
        new Integer32(9),
        new Integer32(10),
        new Integer32(11),
        new Integer32(12),
        new Integer32(13),
        new Integer32(14),
        new Integer32(15),
        new Integer32(16),
        new OctetString("My eth"),
        new TimeTicks(1000)
    };
    Variable[] rowValues2 = new Variable[] {
        new OctetString("Loopback"),
        new Integer32(21),
        new Integer32(22),
        new Integer32(23),
        new Integer32(24),
        new Integer32(25),
        new Integer32(26),
        new Integer32(27),
        new Integer32(28),
        new Integer32(29),
        new Integer32(30),
        new Integer32(31),
        new Integer32(32),
        new Integer32(33),
        new Integer32(34),
        new Integer32(35),
        new Integer32(36),
        new OctetString("My loop"),
        new TimeTicks(2000)
    };
    model.addRow(new DefaultMOMutableRow2PC(new OID("1"), rowValues1));
    model.addRow(new DefaultMOMutableRow2PC(new OID("2"), rowValues2));
    ifXTable.setVolatile(true);
    return ifXTable;
  }

  private static DefaultMOTable createStaticIfTable() {
	    MOTableSubIndex[] subIndexes =
	        new MOTableSubIndex[] { new MOTableSubIndex(SMIConstants.SYNTAX_INTEGER) };
	    MOTableIndex indexDef = new MOTableIndex(subIndexes, false);
	    MOColumn[] columns = new MOColumn[8];
	    int c = 0;
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_INTEGER,
	                     MOAccessImpl.ACCESS_READ_ONLY);     // ifIndex
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_OCTET_STRING,
	                     MOAccessImpl.ACCESS_READ_ONLY);// ifDescr
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_INTEGER,
	                     MOAccessImpl.ACCESS_READ_ONLY);     // ifType
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_INTEGER,
	                     MOAccessImpl.ACCESS_READ_ONLY);     // ifMtu
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_GAUGE32,
	                     MOAccessImpl.ACCESS_READ_ONLY);     // ifSpeed
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_OCTET_STRING,
	                     MOAccessImpl.ACCESS_READ_ONLY);// ifPhysAddress
	    columns[c++] =
	        new MOMutableColumn(c, SMIConstants.SYNTAX_INTEGER,     // ifAdminStatus
	                            MOAccessImpl.ACCESS_READ_WRITE, null);
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_INTEGER,
	                     MOAccessImpl.ACCESS_READ_ONLY);     // ifOperStatus

	    DefaultMOTable ifTable =
	        new DefaultMOTable(new OID("1.3.6.1.2.1.2.2.1"), indexDef, columns);
	    MOMutableTableModel model = (MOMutableTableModel) ifTable.getModel();
	    Variable[] rowValues1 = new Variable[] {
	        new Integer32(1),
	        new OctetString("eth0"),
	        new Integer32(6),
	        new Integer32(1500),
	        new Gauge32(100000000),
	        new OctetString("00:00:00:00:01"),
	        new Integer32(1),
	        new Integer32(1)
	    };
	    Variable[] rowValues2 = new Variable[] {
	        new Integer32(2),
	        new OctetString("loopback"),
	        new Integer32(24),
	        new Integer32(1500),
	        new Gauge32(10000000),
	        new OctetString("00:00:00:00:02"),
	        new Integer32(1),
	        new Integer32(1)
	    };
	    model.addRow(new DefaultMOMutableRow2PC(new OID("1"), rowValues1));
	    model.addRow(new DefaultMOMutableRow2PC(new OID("2"), rowValues2));
	    ifTable.setVolatile(true);
	    return ifTable;
	  }

  private static DefaultMOTable createStaticSnmp4sTable() {
	    MOTableSubIndex[] subIndexes =
	        new MOTableSubIndex[] { new MOTableSubIndex(SMIConstants.SYNTAX_INTEGER) };
	    MOTableIndex indexDef = new MOTableIndex(subIndexes, false);
	    MOColumn[] columns = new MOColumn[8];
	    int c = 0;
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_NULL,
	                     MOAccessImpl.ACCESS_READ_ONLY);     // testNull
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_INTEGER,
	                     MOAccessImpl.ACCESS_READ_ONLY);	// testBoolean
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_INTEGER,
	                     MOAccessImpl.ACCESS_READ_ONLY);     // ifType
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_INTEGER,
	                     MOAccessImpl.ACCESS_READ_ONLY);     // ifMtu
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_GAUGE32,
	                     MOAccessImpl.ACCESS_READ_ONLY);     // ifSpeed
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_OCTET_STRING,
	                     MOAccessImpl.ACCESS_READ_ONLY);// ifPhysAddress
	    columns[c++] =
	        new MOMutableColumn(c, SMIConstants.SYNTAX_INTEGER,     // ifAdminStatus
	                            MOAccessImpl.ACCESS_READ_WRITE, null);
	    columns[c++] =
	        new MOColumn(c, SMIConstants.SYNTAX_INTEGER,
	                     MOAccessImpl.ACCESS_READ_ONLY);     // ifOperStatus

	    DefaultMOTable ifTable =
	        new DefaultMOTable(new OID("1.3.6.1.4.1.50000.1.1"), indexDef, columns);
	    MOMutableTableModel model = (MOMutableTableModel) ifTable.getModel();
	    Variable[] rowValues1 = new Variable[] {
	        new Integer32(1),
	        new OctetString("eth0"),
	        new Integer32(6),
	        new Integer32(1500),
	        new Gauge32(100000000),
	        new OctetString("00:00:00:00:01"),
	        new Integer32(1),
	        new Integer32(1)
	    };
	    Variable[] rowValues2 = new Variable[] {
	        new Integer32(2),
	        new OctetString("loopback"),
	        new Integer32(24),
	        new Integer32(1500),
	        new Gauge32(10000000),
	        new OctetString("00:00:00:00:02"),
	        new Integer32(1),
	        new Integer32(1)
	    };
	    model.addRow(new DefaultMOMutableRow2PC(new OID("1"), rowValues1));
	    model.addRow(new DefaultMOMutableRow2PC(new OID("2"), rowValues2));
	    ifTable.setVolatile(true);
	    return ifTable;
	  }

  protected void initTransportMappings() throws IOException {
    transportMappings = new TransportMapping[1];
    Address addr = GenericAddress.parse(address);
    TransportMapping tm =
        TransportMappings.getInstance().createTransportMapping(addr);
    transportMappings[0] = tm;
  }

  public static TestAgent start(String address) {
    if(address == null) address = "0.0.0.0/161";
    TestAgent testAgent1 = null;
    BasicConfigurator.configure();
    Logger.getRootLogger().setLevel(Level.ERROR);
    try {
      testAgent1 = new TestAgent(new File("SNMP4JTestAgentBC.cfg"),
                                 new File("SNMP4JTestAgentConfig.cfg"));
      testAgent1.address = address;
      testAgent1.init();
      testAgent1.loadConfig(ImportModes.REPLACE_CREATE);
      testAgent1.getServer().addContext(new OctetString("public"));
      testAgent1.finishInit();
      testAgent1.run();
      testAgent1.sendColdStartNotification();

    }
    catch (IOException ex) {
      ex.printStackTrace();
      testAgent1 = null;
    }

    return testAgent1;
  }

  protected void unregisterManagedObjects() {
    // here we should unregister those objects previously registered...
  }

  protected void addCommunities(SnmpCommunityMIB communityMIB) {
    Variable[] com2sec = new Variable[] {
        new OctetString("public"),              // community name
        new OctetString("cpublic"),              // security name
        getAgent().getContextEngineID(),        // local engine ID
        new OctetString("public"),              // default context name
        new OctetString(),                      // transport tag
        new Integer32(StorageType.nonVolatile), // storage type
        new Integer32(RowStatus.active)         // row status
    };
    MOTableRow row =
        communityMIB.getSnmpCommunityEntry().createRow(
          new OctetString("public2public").toSubIndex(true), com2sec);
    communityMIB.getSnmpCommunityEntry().addRow(row);
//    snmpCommunityMIB.setSourceAddressFiltering(true);
  }

  protected void registerSnmpMIBs() {
    heartbeatMIB = new Snmp4jHeartbeatMib(super.getNotificationOriginator(),
                                          new OctetString(),
                                          super.snmpv2MIB.getSysUpTime());
    agentppSimulationMIB = new AgentppSimulationMib();
    super.registerSnmpMIBs();
  }
}