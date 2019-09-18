JMX Interface
=============
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

