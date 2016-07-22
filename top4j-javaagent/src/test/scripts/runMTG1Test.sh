export JAVA_OPTS="-XX:+UseG1GC -Xms4m -Xmx4m -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -verbosegc"
`dirname $0`/runMultiThreadedTest.sh $*
