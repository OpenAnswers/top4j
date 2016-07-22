package io.top4j.javaagent.mbeans.jvm.threads;

import io.top4j.javaagent.mbeans.StatsMXBean;

public interface ThreadStatsMXBean extends StatsMXBean {

	void setCpuUsage(double cpuUsage);

	double getCpuUsage();
	
	void setUserCpuUsage(double userCpuUsage);
	
	double getUserCpuUsage();
	
	void setSysCpuUsage(double sysCpuUsage);
	
	double getSysCpuUsage();
	
	long getThreadCount();
	
	void setThreadCount(long threadCount);

	long getRunnableThreadCount();

	void setRunnableThreadCount(long runnableThreadCount);

	long getBlockedThreadCount();

	void setBlockedThreadCount(long blockedThreadCount);

	long getWaitingThreadCount();

	void setWaitingThreadCount(long waitingThreadCount);

	long getTimedWaitingThreadCount();

	void setTimedWaitingThreadCount(long timedWaitingThreadCount);

}
