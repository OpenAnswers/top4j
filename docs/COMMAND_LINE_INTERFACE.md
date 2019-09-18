Command Line Interface
======================
When running the Top4J CLI jar in Remote Attach mode, the user will be presented with an interactive screen consisting of a header followed by a list of the top 10 threads ordered by CPU utilisation. See screenshot above for more details. A real-time thread stack trace (thread dump) of each of the listed threads can be displayed by entering the number associated with the thread (column 1). Once in the stack trace screen, the user can return to the main menu by typing "m". From the main menu screen, the user can switch to the blocked threads screen by typing "b". The blocked threads screen consists of a header followed by a list of the top 10 blocked threads ordered by the percentage of time that they have been blocked. As before, a real-time thread stack trace of each of the listed blocked threads can be displayed by entering the number associated with the thread (column 1). To leave any of the interactive screens, detach from the remote JVM and exit the top4j CLI, type "q".

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

-hv | -CD -d delay -p pid -S cache-size -T cache-ttl

All command-line switches are optional. White space between command-line switches is also optional.

-h : Help

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Show usage prompt, then quit.

-d : Delay time interval as:  -d ss (seconds)

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Specifies the delay between screen updates, and overrides the default value of 3 seconds.

-p : Monitor PIDs as:  -p pid

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Monitor the JVM process ID specified.

-v : Print configuration properties on start up

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Prints the [Top4J Configuration Properties](/docs/CONFIGURATION.md) used to configure the Top4J Java Agent used to gather performance metrics displayed on the Top4J CLI screens.

-C : Enable thread usage cache

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Switch to enable thread usage cache (enabled by default). The thread usage cache is a performance enhancement used to store the top thread IDs by usage (CPU and blocked time) so that only the threads with a history of high CPU usage or thread contention are updated on each thread usage update. The thread usage cache is updated periodically according to the thread cache time-to-live setting.

-D : Disable thread usage cache

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Switch to disable thread usage cache (which is enabled by default).

-S : Thread usage cache size as: -S size

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Specifies the number of top threads stored within the thread usage cache (500 by default).

-T : Thread usage cache time-to-live as: -T ss (seconds)

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Specifies the number of seconds the thread usage cache will be used before it is refreshed (15 seconds by default).

