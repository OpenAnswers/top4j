#!/bin/bash
#
# jmapCollector.sh
#
#  - collect jmap heap dumps at regular intervals
#

JVM_PID=$1
JMAP_COMMAND="jmap -histo:live"
INTERVAL=600

# check args
if [[ ! ${JVM_PID} =~ [0-9]+ ]]
then
	echo "USAGE: $0 <jvm-pid>"
	exit 1
fi

# check JVM_PID is running
if ! `ps -p ${JVM_PID} | grep -q java`
then
	echo "ERROR: Unable to find JVM process with PID ${JVM_PID}"
	exit 2
fi

echo "Running jmap heap dumps against JVM PID ${JVM_PID}...."
ps -fp ${JVM_PID}

i=1

while true
do
	echo "Taking JVM PID ${JVM_PID} heap dump @ `date`"
	${JMAP_COMMAND} ${JVM_PID} > jmap.${JVM_PID}-${i}.txt
	if [[ $? -gt 0 ]]
	then
		echo "ERROR: Problem running jmap command....exiting"
		exit 3
	fi
	(( i=i+1 ))
	sleep ${INTERVAL}
done

