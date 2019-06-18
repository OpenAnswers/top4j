export JAVA_OPTS="-XX:+UseConcMarkSweepGC -Xms6m -Xmx6m -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -verbosegc"
`dirname $0`/runMultiThreadedTest.sh $*
