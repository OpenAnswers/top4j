java -classpath "../../../target/top4j-javaagent-0.0.1-SNAPSHOT.jar" -Djava.util.logging.config.file=logging.properties ${JAVA_OPTS} io.top4j.javaagent.test.MultiThreadedTest $*
