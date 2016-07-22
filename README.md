Top4J
=====
Top4J is a lightweight, low overhead, production-ready performance analysis tool for the Java runtime environment.

Top4J has two modes of operation: Java Agent and Command Line.

The Top4J Java Agent is invoked via the standard `java -javaagent` command line argument and runs as a background thread inside the target JVM.

The Top4J Command Line Interface attaches to a remote JVM process via the JDK tools VirtualMachine API.

See "Get Started" section for more details.
Build
=====
```bash
git clone git@gitlab.openans.co.uk:ryan/top4j.git
mvn clean package
```
Get Started
===========
**Run as Java Agent**
1. Download and install the Top4J jar within an appropriate location on the local file system.
1. Add -javaagent command line argument to JVM startup args, e.g. `java -javaagent:<path-to-top4j-jar>/top4j-javaagent-1.0.1.jar <java-class-name>`
**Run Command Line Interface**
1. Download and install the Top4J jar within an appropriate location on the local file system.
1. Run Top4J jar specifying JVM PID as the first argument, e.g. `java -jar top4j-cli-0.0.1.jar 12345`
