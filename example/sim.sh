#! /bin/sh

HOME_DIR=~
SIM_CP="$HOME_DIR/.ivy2/cache/org.snmp4j/snmp4j/jars/snmp4j-2.2.1.jar:$HOME_DIR/.ivy2/cache/org.snmp4j/snmp4j-agent/jars/snmp4j-agent-2.0.7.jar:$HOME_DIR/.ivy2/cache/log4j/log4j/jars/log4j-1.2.14.jar"

java -cp $SIM_CP org.snmp4j.agent.test.TestAgent
