package io.top4j.javaagent.mbeans.jvm.threads;

import io.top4j.javaagent.profiler.CpuTime;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.util.Map;
import java.util.logging.*;

public class ThreadStats implements ThreadStatsMXBean {
	
	private ThreadUsage threadUsage;
    private CpuTime cpuTime = new CpuTime();
	private double mBeanCpuTime;
    private boolean hotMethodProfilingEnabled;

	private static final Logger LOGGER = Logger.getLogger(ThreadStats.class.getName());
	
	public ThreadStats ( MBeanServerConnection mbsc, Map<Integer, TopThread> topThreadsMap ) throws IOException {
		
		LOGGER.fine("Initialising Thread Stats....");

        this.hotMethodProfilingEnabled = false;
		// instantiate new ThreadUsage object used to track thread usage
		this.threadUsage = new ThreadUsage( mbsc, topThreadsMap );
		
	}

	public ThreadStats ( MBeanServerConnection mbsc, Map<Integer, TopThread> topThreadsMap, Map<Integer, BlockedThread> blockedThreadsMap ) throws IOException {

		LOGGER.fine("Initialising Thread Stats with thread contention monitoring enabled....");

        this.hotMethodProfilingEnabled = false;
		// instantiate new ThreadUsage object used to track thread usage
		this.threadUsage = new ThreadUsage(mbsc, topThreadsMap, blockedThreadsMap);

	}

	public  ThreadStats ( MBeanServerConnection mbsc, Map<Integer, TopThread> topThreadsMap, HotMethods hotMethods, long hotMethodPollInterval ) throws IOException {

		LOGGER.fine("Initialising Thread Stats with hot method profiling enabled....");

        this.hotMethodProfilingEnabled = true;
		// instantiate new ThreadUsage object used to track thread usage
		this.threadUsage = new ThreadUsage(mbsc, topThreadsMap, hotMethods, hotMethodPollInterval);

	}

	public ThreadStats ( MBeanServerConnection mbsc, Map<Integer, TopThread> topThreadsMap, Map<Integer, BlockedThread> blockedThreadsMap, HotMethods hotMethods, long hotMethodPollInterval ) throws IOException {

		LOGGER.fine("Initialising Thread Stats with thread contention monitoring and hot method profiling enabled....");

        this.hotMethodProfilingEnabled = true;
		// instantiate new ThreadUsage object used to track thread usage
		this.threadUsage = new ThreadUsage(mbsc, topThreadsMap, blockedThreadsMap, hotMethods, hotMethodPollInterval);

	}


	/** Update Thread stats. */
    public synchronized void update( ) {

        // initialise thread CPU timer
    	cpuTime.init();

    	LOGGER.fine("Updating Thread Stats....");

    	// update CPU usage
    	threadUsage.update();

        // update thread stats CPU time
        if (hotMethodProfilingEnabled) {
            mBeanCpuTime = cpuTime.getMillis() + threadUsage.getHotMethods().getAndResetMBeanCpuTime();
        }
        else {
            mBeanCpuTime = cpuTime.getMillis();
        }

    }

	@Override
	public void setMBeanCpuTime(double agentCpuTime) {
        this.mBeanCpuTime = agentCpuTime;
	}

	@Override
	public double getMBeanCpuTime() {
		return mBeanCpuTime;
	}

	@Override
	public void setCpuUsage(double threadUsage) {
		this.threadUsage.setCpuUsage(threadUsage);
	}

	@Override
	public double getCpuUsage() {
		return this.threadUsage.getCpuUsage();
	}

	@Override
	public void setUserCpuUsage(double userCpuUsage) {
		this.threadUsage.setUserCpuUsage(userCpuUsage);
	}

	@Override
	public double getUserCpuUsage() {
		return this.threadUsage.getUserCpuUsage();
	}

	@Override
	public void setSysCpuUsage(double sysCpuUsage) {
		this.threadUsage.setSysCpuUsage(sysCpuUsage);
	}

	@Override
	public double getSysCpuUsage() {
		return this.threadUsage.getSysCpuUsage();
	}

	@Override
	public long getThreadCount() {
		return this.threadUsage.getThreadCount();
	}

	@Override
	public void setThreadCount(long threadCount) {
		this.threadUsage.setThreadCount(threadCount);
	}

	@Override
	public long getRunnableThreadCount() {
		return this.threadUsage.getRunnableThreadCount();
	}

	@Override
	public void setRunnableThreadCount(long runnableThreadCount) {
		this.threadUsage.setRunnableThreadCount(runnableThreadCount);
	}

	@Override
	public long getBlockedThreadCount() {
		return this.threadUsage.getBlockedThreadCount();
	}

	@Override
	public void setBlockedThreadCount(long blockedThreadCount) {
		this.threadUsage.setBlockedThreadCount(blockedThreadCount);
	}

	@Override
	public long getWaitingThreadCount() {
		return this.threadUsage.getWaitingThreadCount();
	}

	@Override
	public void setWaitingThreadCount(long waitingThreadCount) {
		this.threadUsage.setWaitingThreadCount(waitingThreadCount);
	}

	@Override
	public long getTimedWaitingThreadCount() {
		return this.threadUsage.getTimedWaitingThreadCount();
	}

	@Override
	public void setTimedWaitingThreadCount(long timedWaitingThreadCount) {
		this.threadUsage.setTimedWaitingThreadCount(timedWaitingThreadCount);
	}

}
