. `dirname $0`/setenv.sh
java -classpath "../../../target/top4j-javaagent-0.0.1-SNAPSHOT.jar" ${JAVA_OPTS} $@ io.top4j.javaagent.utils.GarbageCollectorNames
