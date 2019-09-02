Top4J
=====
Top4J is a lightweight, low overhead, production-ready performance analysis tool for the Java runtime environment. As the name suggests, it works a bit like the UNIX/Linux top command but at the JVM-level exposing key performance metrics from a Java runtime perspective.

Top4J has two modes of operation: Java Agent and Remote Attach CLI.

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
1. Download and install the [Top4J Java Agent jar](http://hlcit003:8081/nexus/content/repositories/releases/io/top4j/top4j-javaagent/0.0.7/top4j-javaagent-0.0.7.jar) within an appropriate location on the local file system.
1. Add -javaagent command line argument to JVM startup args, e.g. `java -javaagent:<path-to-top4j-jar>/top4j-javaagent-0.0.7.jar <java-class-name>`

**Run Top4J via CLI Remote Attach feature**
1. Download and install the [Top4J CLI jar](http://hlcit003:8081/nexus/content/repositories/releases/io/top4j/top4j-cli/0.0.7/top4j-cli-0.0.7.jar) within an appropriate location on the local file system.
1. Run Top4J CLI jar specifying JVM PID as the first argument, e.g. `java -jar top4j-cli-0.0.7.jar -p 12345`, **OR** with no arguments to auto-detect running JVMs and select from a list, e.g. `java -jar top4j-cli-0.0.7.jar`

   **NOTE1:** The top4j CLI jar **must** be run as the target JVM process owner.
   **NOTE2:** The top4j CLI jar **must** be run using a Java JDK install not a JRE.

Screenshots
===========
**Screenshot of Top4J using Remote Attach command line interface:**
```bash
top4j - 16:12:18 up 2 days,  load average: 1.43
Attached to: org.mule.module.reboot.MuleContainerBootstrap start0 [PID=7448]
Threads: 1018 total,   162 runnable,   292 waiting,   564 timed waiting,   0 blocked
%Cpu(s): 32.29 total,  22.43 user,  9.85 sys
Heap Util(%):        0.00 eden,        57.58 survivor,        48.77 tenured
Mem Alloc(MB/s):     55.76 eden,        3.64 survivor,        0.25 tenured
GC Overhead(%):      1.2930

Top Threads:

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

Command Line Interface
======================
When running the top4j CLI jar in Remote Attach mode, the user will be presented with an interactive screen consisting of a header followed by a list of the top 10 threads ordered by CPU utilisation. See screenshot above for more details. A real-time thread stack trace (thread dump) of each of the listed threads can be displayed by entering the number associated with the thread (column 1). Once in the stack trace screen, the user can return to the main menu by typing "m". From the main menu screen, the user can switch to the blocked threads screen by typing "b". The blocked threads screen consists of a header followed by a list of the top 10 blocked threads ordered by the percentage of time that they have been blocked. As before, a real-time thread stack trace of each of the listed blocked threads can be displayed by entering the number associated with the thread (column 1). To leave any of the interactive screens, detach from the remote JVM and exit the top4j CLI, type "q".

Each of the fields and columns displayed by the top4J CLI are detailed below.

1. Header Fields
----------------

**Load average:** The system load average for the last minute. The system load average is the sum of the number of runnable entities queued to the available processors plus the number of runnable entities running on the available processors averaged over a period of time. The way in which the load average is calculated is operating system specific but is typically a damped time-dependent average. If the load average is not available, a negative value is returned.

**Attached to:** The display name and process ID of the attached Java process.

**Threads:** The total number of threads within the JVM process along with a breakdown of the thread states. See the [java.lang.Thread.State Enum](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html) for more details.

**%Cpu(s):** The JVM process percentage CPU utilisation across all available processors along with a breakdown of the user space and system CPU included in the total. This is the JVM process CPU utilisation at the system level. It's effectively the sum of the CPU utilisation of all threads running within the JVM process divided by the number of available processors.

**Heap Util(%):** The JVM heap space utilisation following the most recent garbage collection event for each of the primary Java heap spaces. This is effectively the residual heap occupied by live objects within the JVM heap which can't be garbage collected because they are still referenced by one or more other objects. It is calculated as ( ( heapUsed / heapCommitted ) * 100 ). See HeapStats MBean below for more details.

**Mem Alloc(MB/s):** The memory allocation rate represents the amount of memory consumed by the Java application whilst creating new objects over time. It is measured in megabytes per second (MB/s). The memory allocation rate within the eden heap space is driven by the creation of new objects. The memory allocation rate within the survivor and tenured heap spaaces is generally caused by the promotion of tenured or long-lived objects from eden to survivor and survivor to tenured. Within a healthy system, most of the memory allocation should occur within the eden space with decreasing levels of allocation to the survivor and tenured spaces. See the MemoryStats MBean below for more details.

**GC Overhead(%):** The GC overhead is calculated as the percentage of real time (wall clock time) the JVM spends in garbage collection. Only stop-the-world garbage collection pauses contribute to the GC overhead. This, therefore, equates to the percentage of real time that the application is stopped whilst garbage collection takes place. This is a key performance indicator of the impact of garbage collection on a running Java application. A high GC overhead overhead can lead to poor application performance as there is less time available to process application tasks and application threads can be blocked waiting to allocate memory (i.e. create objects).

2. Columns
----------

    **#** : The Thread Number
    Used to select the thread in order to view the thread stack trace.

    **TID** : Thread ID
    The thread's unique process ID, which periodically wraps, though never restarting at zero.

    **S** : Thread State
    The thread state as defined by the [java.lang.Thread.State Enum](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html).
    
    **%CPU** : CPU Usage
    The thread percentage CPU utilisation.

    **%BLOCKED** : Blocked Time
    The percentage of time that the thread has been in a blocked state during the last iteration.

    **THREAD NAME** : Thread Name
    The thread name.

3. COMMAND-LINE Options
-----------------------

The command-line syntax for top4j consists of:

         -h | -d delay -p pid

    -h : Help
         Show usage prompt, then quit.

    -d : Delay time interval as:  -d ss (seconds)
         Specifies the delay between screen updates, and overrides the default value of 3 seconds.

    -p : Monitor PIDs as:  -p pid
         Monitor the JVM process ID specified.

MBeans
======
Top4J performance metrics are exposed via JMX MBean attributes. All Top4J MBeans can be found under the "io.top4j" JMX domain.
A complete list of Top4J MBeans and their associated attributes is documented below. The MBean Object Name is provided in square brackets.
**All MBeans**
--------------
**Description:** The following MBean attributes and operations are common to all top4j MBeans....

### Attributes

**MBeanCpuTime:** The amount of CPU time in milliseconds consumed by the MBean update() operation per invocation.

### Operations

**update():** Update MBean attributes with latest data. 

**AgentStats** [io.top4j:type=Agent,statsType=AgentStats]
---------------------------------------------------------
**Description:** Used to persist and expose stats relating to the top4J JavaAgent run-time.

### Attributes

**AgentCpuTime:** The amount of CPU time in milliseconds consumed by the top4J JavaAgent background threads.

**AgentCpuUtil:** The percentage CPU utilisation of the top4J JavaAgent background threads.

**Iterations:** The number of top4J JavaAgent stats update iterations since the JavaAgent was enabled.

**BlockedThread:** [io.top4j:type=JVM,statsType=BlockedThread,rank=N]
---------------------------------------------------------------------
**Description:** Used to persist and expose stats relating to a thread that has been blocked for some time during the last iteration. Each blocked thread is ranked between 1 and N via the "rank" attribute on the BlockedThread MBean Object Name, where 1 is the thread that has been blocked the most and N is the thread that has been blocked the least (out of the top ranked blocked threads).

### Attributes

**ThreadName:** The thread name.

**ThreadId:** The thread ID.

**ThreadState:** The thread state as defined by the [java.lang.Thread.State Enum](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html).

**ThreadBlockedTime:** The time in milliseconds that the thread has been in a blocked state during the last iteration.

**ThreadBlockedPercentage:** The percentage of time that the thread has been in a blocked state during the last iteration. A high thread blocked percentage can be an indicator of thread lock contention, e.g. threads blocked waiting to access a synchronised method or code block.

### Operations

**getStackTrace(int maxDepth):** Get stack trace for thread with a maximum frame depth of maxDepth.

**getStackTraceWithContext(int maxDepth):** Get stack trace for thread with context, e.g. thread name and thread state, with a maximum frame depth of maxDepth.

**GCStats** [io.top4j:type=JVM,statsType=GCStats]
-------------------------------------------------
**Description:** Used to persist and expose stats relating to the performance of the JVM Garbage Collector.
### Attributes

**GcOverhead:** The GC overhead is calculated as the percentage of real time (wall clock time) the JVM spends in garbage collection. Only stop-the-world garbage collection pauses contribute to the GC overhead. This, therefore, equates to the percentage of real time that the application is stopped whilst garbage collection takes place. This is a key performance indicator of the impact of garbage collection on a running Java application. A high GC overhead overhead can lead to poor application performance as there is less time available to process application tasks and application threads can be blocked waiting to allocate memory (i.e. create objects).

**MeanNurseryGCTime:** The mean time in milliseconds spent during a single nursery or eden or new stop-the-world GC event during the last iteration. This time is not available for application processing and should therefore be kept to a minimum.

**MeanTenuredGCTime:** The mean time in milliseconds spent during a single tenured or full or old stop-the-world GC event during the last iteration. This time is not available for application processing and should therefore be kept to a minimum.

**HeapStats** [io.top4j:type=JVM,statsType=HeapStats]
-----------------------------------------------------
**Description:** Used to persist and expose stats relating to the JVM heap utilisation. The heap utilisation is calculated as the percentage of heap used following the most recent garbage collection event. In other words.... ( heapUsed / heapCommitted ) * 100.

### Attributes

**EdenSpaceUtil:** The eden (or nursery or new) heap space utilisation following the most recent garbage collection event. This is effectively the residual heap occupied by live objects within the eden space which can't be garbage collected because they are still referenced by one or more other objects. The eden heap utilisation is typically very low as most objects created within the eden heap space are either garbage collected (cleared up) or promoted to one of the survivor spaces at each nursery GC event.

**SurvivorSpaceUtil:** The survivor heap space utilisation following the most recent garbage collection event. This is effectively the residual heap occupied by live objects within the survivor spaces which can't be garbage collected because they are still referenced by one or more other objects.

**TenuredHeapUtil:** The tenured (or old) heap space utilisation following the most recent garbage collection event. This is effectively the residual heap occupied by live objects within the tenured (or old) space which can't be garbage collected because they are still referenced by one or more other objects. High tenured heap space utilisation can be an indication that the JVM is running low on memory. A high tenured heap utilisation can lead to frequent garbage collection events which will typically lead to a high GC overhead and therefore poor application performance/memory throughput. See GCOverhead attribute above for more details.

**HotMethod** [io.top4j:type=JVM,statsType=HotMethod]
-----------------------------------------------------
**Description:** Used to persist and expose stats relating to a Java method that has been determined to have executed frequently.

### Attributes

**MethodName:** The method name.

**ThreadName:** The name of the thread that has executed the method most recently.

**ThreadId:** The ID of the thread that has executed the method most recently.

**LoadProfile:** An indication of how hot the method has been during the last iteration. The load profile is calculated as the CPU time used by a top thread that has executed a hot method as a percentage of the total CPU time used by all top threads during the last iteration.

### Operations

**getStackTrace():** The most recent stack trace for the hot method.

**MemoryStats** [io.top4j:type=JVM,statsType=MemoryStats]
---------------------------------------------------------
**Description:** Used to persist and expose stats relating to the JVM memory pool usage.

### Attributes

**MemoryAllocationRate:** The memory allocation rate represents the amount of memory consumed by the Java application whilst creating new objects over time. It is measured in MB per second (MB/s). A high memory allocation rate can be an indication that the Java application is creating too many new objects and as a result putting pressure on the JVM memory management sub-system which can cause more frequent GC events and associated GC overhead.

**MemorySurvivorRate:** The memory survivor rate represents the amount of memory that survives a nursery (or new) GC event and is promoted to one of the survivor spaces over time. It is measured in MB per second (MB/s). A high memory survivor rate can be an indication that too many objects are being promoted to the survivor spaces which can be an indication that the eden space is undersized or the memory allocation rate (to eden) is too high.

**MemoryPromotionRate:** The memory promotion rate represents the amount of memory that survives one or more nursery (or new) GC events and is promoted to the tenured (or old) space over time. It is measured in MB per second (MB/s). A high memory promotion rate can be an indication that too many objects are being promoted to the tenured space which can be an indication that the eden space is undersized or the memory allocation rate (to eden) is too high.


**ThreadStats** [io.top4j:type=JVM,statsType=ThreadStats]
---------------------------------------------------------
**Description:** Used to persist and expose stats relating to JVM thread usage.

### Attributes

**CpuUsage:** The combined CPU usage of all JVM threads as a percentage of the total CPU available during the last iteration. The CPU usage is calculated as the total CPU usage consumed by all JVM threads divided by the number of processor cores available to give the system level CPU usage for the JVM process.

**UserCpuUsage:** The combined user space CPU usage of all JVM threads as a percentage of the total CPU available during the last iteration.

**SysCpuUsage:** The combined system CPU usage of all JVM threads as a percentage of the total CPU available during the last iteration.

**ThreadCount:** The total number of threads running within the JVM process at the time of the last stats update.

**RunnableThreadCount:** The total number of runnable threads within the JVM process at the time of the last stats update.

**BlockedThreadCount:** The total number of blocked threads within the JVM process at the time of the last stats update.

**WaitingThreadCount:** The total number of waiting threads within the JVM process at the time of the last stats update.

**TimedWaitingThreadCount:** The total number of timed waiting threads within the JVM process at the time of the last stats update.

**TopThread** [io.top4j:type=JVM,statsType=TopThread,rank=N]
------------------------------------------------------------
**Description:** Used to persist and expose stats relating to a thread that has been determined as a top CPU consumer during the last iteration. Each top thread is ranked between 1 and N via the "rank" attribute on the TopThread MBean Object Name, where 1 is the thread that has consumed the most CPU and N is the thread that has consumed the least (out of the top ranked threads).

### Attributes

**ThreadName:** The thread name.

**ThreadId:** The thread ID.

**ThreadState:** The thread state as defined by the [java.lang.Thread.State Enum](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html).

**ThreadCpuUsage:** The thread CPU time as a percentage of the total CPU time available during the last iteration. The thread CPU usage is calculated as the total CPU time consumed by a thread divided by the total CPU time available (wall clock time) multiplied by 100. In other words.... ( threadCpuTime / elapsedTime ) * 100

### Operations

**getStackTrace(int maxDepth):** Get stack trace for thread with a maximum frame depth of maxDepth.

**getStackTraceWithContext(int maxDepth):** Get stack trace for thread with context, e.g. thread name and thread state, with a maximum frame depth of maxDepth.

