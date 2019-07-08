Top4J
=====
Top4J is a lightweight, low overhead, production-ready performance analysis tool for the Java runtime environment. As the name suggests, it works a bit like the UNIX/Linux top command but at the JVM-level exposing key performance metrics from a Java runtime perspective.

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
1. Download and install the [Top4J Java Agent jar](http://hlcit003:8081/nexus/content/repositories/releases/io/top4j/top4j-javaagent/0.0.6/top4j-javaagent-0.0.6.jar) within an appropriate location on the local file system.
1. Add -javaagent command line argument to JVM startup args, e.g. `java -javaagent:<path-to-top4j-jar>/top4j-javaagent-0.0.6.jar <java-class-name>`

**Run Top4J via Remote Attach feature**
1. Download and install the [Top4J CLI jar](http://hlcit003:8081/nexus/content/repositories/releases/io/top4j/top4j-cli/0.0.6/top4j-cli-0.0.6.jar) within an appropriate location on the local file system.
1. Run Top4J CLI jar specifying JVM PID as the first argument, e.g. `java -jar top4j-cli-0.0.6.jar 12345`, **OR** with no arguments to auto-detect running JVMs and select from a list, e.g. `java -jar top4j-cli-0.0.6.jar`

   **NOTE:** The top4j CLI jar **must** be run as the target JVM process owner.

Screenshots
===========
**Screenshot of Top4J using Remote Attach command line interface:**
```bash
top4j - 16:12:18 up 2 days,  load average: 1.43
Threads: 1018 total,   162 runnable,   292 waiting,   564 timed waiting,   0 blocked
%Cpu(s): 32.29 total,  22.43 user,  9.85 sys
Heap Util(%):        0.00 eden,        57.58 survivor,        48.77 tenured
Mem Alloc(MB/s):     55.76 eden,        3.64 survivor,        0.25 tenured
GC Overhead(%):      1.2930

#  TID     S  %CPU  THREAD NAME
=  ===     =  ====  ===========
0  1085    T  14.4  [c4nl-mediator-service-ws-1.6.51].c4nl-mediator-
1  1078    R  14.3  [c4nl-mediator-service-ws-1.6.51].c4nl-mediator-
2  43      W  5.2   hz.1.operation.thread-3
3  42      W  5.2   hz.1.operation.thread-2
4  41      W  5.1   hz.1.operation.thread-1
5  40      W  5.0   hz.1.operation.thread-0
6  556716  T  3.9   RMI TCP Connection(6263)-172.26.29.101
7  556713  R  2.7   RMI TCP Connection(6261)-172.26.29.101
8  27      R  1.5   hz.1.IO.thread-out-1
9  26      R  1.1   hz.1.IO.thread-in-1


Hit [0-9] to view thread stack trace, [b] to view blocked threads, [q] to quit
```

MBeans
======
Top4J performance metrics are exposed via JMX MBean attributes. All Top4J MBeans can be found under the "io.top4j" JMX domain.
A complete list of Top4J MBeans and their associated attributes is documented below. The MBean Object Name is provided in square brackets.
**All MBeans**
--------------
**Description:** The following MBean attributes and operations are common to all top4j MBeans....
**Attributes:**
**MBeanCpuTime:** The amount of CPU time in milliseconds consumed by the MBean update() operation per invocation.
**Operations:**
**update():** Update MBean attributes with latest data. 
**AgentStats** [io.top4j:type=Agent,statsType=AgentStats]
---------------------------------------------------------
**Description:** Used to store stats relating to the top4J JavaAgent run-time.
**Attributes:**
**AgentCpuTime:** The amount of CPU time in milliseconds consumed by the top4J JavaAgent background threads.
**AgentCpuUtil:** The percentage CPU utilisation of the top4J JavaAgent background threads.
**Iterations:** The number of top4J JavaAgent stats update iterations since the JavaAgent was enabled.
**BlockedThread:** [io.top4j:type=JVM,statsType=BlockedThread,rank=N]
---------------------------------------------------------------------
**Description:** Used to store stats relating to a thread that has been blocked for some time during the last iteration.
**Attributes:**
**ThreadName:** The thread name.
**ThreadId:** The thread ID.
**ThreadState:** The thread state as defined by the [java.lang.Thread.State Enum](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html).
**ThreadBlockedTime:** The time in milliseconds that the thread has been in a blocked state during the last iteration.
**ThreadBlockedPercentage:** The percentage of time that the thread has been in a blocked state during the last iteration.
**Operations:**
**getStackTrace(int maxDepth):** Get stack trace for thread with a maximum frame depth of maxDepth.
**getStackTraceWithContext(int maxDepth):** Get stack trace for thread with context, e.g. thread name and thread state, with a maximum frame depth of maxDepth.
**GCStats** [io.top4j:type=JVM,statsType=GCStats]
-------------------------------------------------
**Description:**
**Attributes:**
**Operations:**
**HeapStats** [io.top4j:type=JVM,statsType=HeapStats]
-----------------------------------------------------
**Description:**
**Attributes:**
**Operations:**
**HotMethod** [io.top4j:type=JVM,statsType=HotMethod]
-----------------------------------------------------
**Description:**
**Attributes:**
**Operations:**
**MemoryStats** [io.top4j:type=JVM,statsType=MemoryStats]
---------------------------------------------------------
**Description:**
**Attributes:**
**Operations:**
**StatsLogger** [io.top4j:type=JVM,statsType=StatsLogger]
---------------------------------------------------------
**Description:**
**Attributes:**
**Operations:**
**ThreadStats** [io.top4j:type=JVM,statsType=ThreadStats]
---------------------------------------------------------
**Description:**
**Attributes:**
**Operations:**
**TopThread** [io.top4j:type=JVM,statsType=TopThread,rank=N]
------------------------------------------------------------
**Description:**
**Attributes:**
**Operations:**

