Logging
=======
The Top4J Java Agent can be configured to log performance metrics to the local file system.

This feature is disabled by default but can be easily enabled via the Top4J configuration property `stats.logger.enabled=true`.

The `stats.logger.enabled` override property can be set in one of two ways:

1. By creating a custom top4j.properties file containing `stats.logger.enabled=true` - see the [Top4J Configuration](/CONFIGURATION.md) documentation for more details.
1. By adding an additional argument to the `java -javaagent` command-line argument, for example....

```bash
java -javaagent:<path-to-top4j-jar>/top4j-javaagent-0.0.8.jar=stats.logger.enabled=true <java-class-name>
```

The Top4J stats logger will log to a directory called `top4j-stats` within the JVMs current working directory by default. However, the default location can be overridden via the Top4J configuration property `stats.logger.directory`, for example....

```bash
java -javaagent:<path-to-top4j-jar>/top4j-javaagent-0.0.8.jar=stats.logger.enabled=true,stats.logger.directory=/var/log/top4j-stats <java-class-name>
```

**NOTE:** Make sure the `stats.logger.directory` is writable by the Java process owner.

The Top4J stats logger creates one log file per [Top4J MBean](/JMX_INTERFACE.md) per day. The stats (MBean attributes) are recorded as comma-separated values (CSV) by default. The stats log files are automatically rolled at midnight.

Here is an example Top4J stats file listing....

```bash
-bash-4.1$ ls -1 *20190917.csv
AgentStats.20190917.csv
BlockedThread-1.20190917.csv
BlockedThread-10.20190917.csv
BlockedThread-2.20190917.csv
BlockedThread-3.20190917.csv
BlockedThread-4.20190917.csv
BlockedThread-5.20190917.csv
BlockedThread-6.20190917.csv
BlockedThread-7.20190917.csv
BlockedThread-8.20190917.csv
BlockedThread-9.20190917.csv
GCStats.20190917.csv
HeapStats.20190917.csv
MemoryStats.20190917.csv
ThreadStats.20190917.csv
TopThread-1.20190917.csv
TopThread-10.20190917.csv
TopThread-2.20190917.csv
TopThread-3.20190917.csv
TopThread-4.20190917.csv
TopThread-5.20190917.csv
TopThread-6.20190917.csv
TopThread-7.20190917.csv
TopThread-8.20190917.csv
TopThread-9.20190917.csv
```

Here are some example Top4J stats....

```bash
-bash-4.1$ head ThreadStats.20190917.csv
Timestamp,BlockedThreadCount,CpuUsage,MBeanCpuTime,RunnableThreadCount,SysCpuUsage,ThreadCount,TimedWaitingThreadCount,UserCpuUsage,WaitingThreadCount
2019-09-17T00:00:02.685+0100,999,0.8090,473.8873,3,0.1449,1007,2,0.6641,3
2019-09-17T00:01:02.407+0100,999,0.5729,315.9774,3,0.1210,1007,2,0.4519,3
2019-09-17T00:02:02.455+0100,999,0.6128,345.2138,3,0.1131,1007,2,0.4996,3
2019-09-17T00:03:02.450+0100,999,0.6369,357.5654,3,0.1035,1007,2,0.5334,3
2019-09-17T00:04:02.400+0100,999,0.5635,314.5569,3,0.0965,1007,2,0.4670,3
2019-09-17T00:05:02.755+0100,999,0.9344,570.6357,3,0.1718,1007,2,0.7626,3
2019-09-17T00:06:02.343+0100,999,0.5960,265.6631,3,0.0933,1007,2,0.5027,3
2019-09-17T00:07:02.400+0100,999,0.5593,314.1110,3,0.1097,1007,2,0.4496,3
2019-09-17T00:08:02.461+0100,999,0.6450,368.6486,3,0.1289,1007,2,0.5162,3
```


