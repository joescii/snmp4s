#! /bin/sh

HOME_DIR=~
SIM_CP="$HOME_DIR/.ivy2/cache/org.snmp4j/snmp4j/jars/snmp4j-2.5.2.jar:$HOME_DIR/.ivy2/cache/org.snmp4j/snmp4j-agent/jars/snmp4j-agent-2.5.3.jar:$HOME_DIR/.ivy2/cache/log4j/log4j/jars/log4j-1.2.14.jar"

java -cp $SIM_CP org.snmp4j.agent.test.TestAgent
