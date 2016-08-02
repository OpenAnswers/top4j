Top4J
=====
Top4J is a lightweight, low overhead, production-ready performance analysis tool for the Java runtime environment.

Top4J has two modes of operation: Java Agent and Remote Attach.

The Top4J Java Agent is invoked via the standard `java -javaagent` command line argument and runs as a background thread inside the target JVM.

The Top4J Remote Attach function is executed via the command line and attaches to a remote JVM process via the JDK tools VirtualMachine API.

See "Get Started" section for more details.
Build
=====
```bash
git clone git@gitlab.openans.co.uk:open-answers/top4j.git
mvn clean package
```
Release
=======
The Top4J continuous build and release Jenkins job can be found [here](http://hlcit001:8080/jenkins/job/top4j/).
Get Started
===========
**Run Top4J as a Java Agent**
1. Download and install the [Top4J Java Agent jar](http://hlcit003:8081/nexus/content/repositories/releases/io/top4j/top4j-javaagent/0.0.1/top4j-javaagent-0.0.1.jar) within an appropriate location on the local file system.
1. Add -javaagent command line argument to JVM startup args, e.g. `java -javaagent:<path-to-top4j-jar>/top4j-javaagent-0.0.1.jar <java-class-name>`

**Run Top4J via Remote Attach feature**
1. Download and install the [Top4J CLI jar](http://hlcit003:8081/nexus/content/repositories/releases/io/top4j/top4j-cli/0.0.1/top4j-cli-0.0.1.jar) within an appropriate location on the local file system.
1. Run Top4J CLI jar specifying JVM PID as the first argument, e.g. `java -jar top4j-cli-0.0.1.jar 12345`

Screenshots
===========
**Screenshot of Top4J using Remote Attach command line interface:**
```bash
top4j - 14:08:48 up 1479607 secs,  load average: 0.67
Threads: 884 total,   128 runnable,   308 waiting,   448 timed waiting,   0 blocked
%Cpu(s): 39.10 total,  27.61 user,  11.49 sys
Heap Util(%):        0.00 eden,        14.31 survivor,        35.75 tenured
Mem Alloc(MB/s):     123.51 eden,        4.38 survivor,        0.35 tenured
GC Stats:  1.1463% GC overhead

#  TID     THREAD NAME                             %CPU
0  913     [c4nl-mediator-service-ws-1.6.2].c4nl-mediator-service-ws-materialSearch.event.correlator19.6
1  924     [c4nl-mediator-service-ws-1.6.2].c4nl-mediator-service-ws-transferMakeRequests.event.correlator19.5
2  41      hz.1.operation.thread-3                 6.8
3  42      hz.1.operation.thread-2                 6.7
4  39      hz.1.operation.thread-0                 6.6
5  40      hz.1.operation.thread-1                 6.4
6  1115425 RMI TCP Connection(19768)-172.26.29.131 4.7
7  9       AsyncLoggerConfig-1                     1.3
8  1115477 ActiveMQ Session Task-54812             1.2
9  1115479 ActiveMQ Session Task-54814             1.0
```

