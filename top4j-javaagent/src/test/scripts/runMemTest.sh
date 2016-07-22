#!/bin/bash
#
# runMemTest.sh
#

HEAP_SIZE=$1

if [[ -z $HEAP_SIZE ]]
then
	HEAP_SIZE=128
fi

java -Xms${HEAP_SIZE}m -Xmx${HEAP_SIZE}m -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -verbosegc  -Djava.util.logging.config.file=logging.properties -Dcom.sun.management.jmxremote -javaagent:../../../target/top4j-javaagent-0.0.1-SNAPSHOT.jar io.top4j.javaagent.test.MemTest
