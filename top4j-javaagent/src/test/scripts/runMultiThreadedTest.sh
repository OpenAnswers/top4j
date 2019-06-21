#!/bin/bash
#
# runMultiThreadedTest.sh
#
#  - run Top4J multi-threaded test harness
#
#    Arguments:
#
#      1. NUM_THREADS: Number of threads, e.g. 10
#      2. NUM_ITERATIONS: Number of iterations per thread, e.g. 100
#      3. PAUSE_TIME: Pause time between iterations in milliseconds, e.g. 10
#
#    Example Usage:
#
#      ./runMultiThreadedTest.sh 10 100 10
#

TOP4J_JAVAAGENT_JAR=`ls ../../../target/top4j-javaagent-*.jar`
ARG_COUNT=$#
NUM_THREADS=$1
NUM_ITERATIONS=$2
PAUSE_TIME=$3

if [[ ! -f ${TOP4J_JAVAAGENT_JAR} ]]
then
	echo "ERROR: Unable to find Top4J java agent jar."
	echo "HINT: Try building the top4j-javaagent Maven project via \"mvn clean package\""
	exit 1
fi

if [[ ${ARG_COUNT} -ne 3 ]]
then
	echo "USAGE: <num-threads> <num-iterations> <pause-time>"
	exit 1
fi

java -classpath "${TOP4J_JAVAAGENT_JAR}" -javaagent:${TOP4J_JAVAAGENT_JAR}=test.property=test,top.thread.count=10 -Djava.util.logging.config.file=logging.properties ${JAVA_OPTS} io.top4j.javaagent.test.MultiThreadedTest ${NUM_THREADS} ${NUM_ITERATIONS} ${PAUSE_TIME}

