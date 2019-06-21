package io.top4j.javaagent.mbeans.jvm.threads;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.Thread.State;
import java.lang.management.*;
import java.util.*;

import java.util.logging.*;

public class ThreadUsage {

    private class ThreadInfo {
	        public long id;
            public String name;
            public State state;
            public boolean active;
            public long interval;
	        public long startTime;
	        public long startCpuTime;
	        public long startUserTime;
	        public long endTime;
	        public long endCpuTime;
	        public long endUserTime;
            public long lastBlockedTime;
            public long intervalBlockedTime;
	        public double cpuUsage;
	        public double userCpuUsage;
	    }

    private int numberOfProcessors;
    private final HashMap<Long,ThreadInfo> threadHistory =
        new HashMap<>( );
    volatile private double cpuUsage;
    volatile private double userCpuUsage;
    volatile private double sysCpuUsage;
	private ThreadTimeMap cpuTimeMap;
    private ThreadTimeMap blockedTimeMap;
	volatile private long threadCount;
    volatile private long runnableThreadCount;
    volatile private long blockedThreadCount;
    volatile private long waitingThreadCount;
    volatile private long timedWaitingThreadCount;
	private final ThreadMXBean threadMXBean;
	private Map<Integer, TopThread> topThreadsMap;
    private Map<Integer, BlockedThread> blockedThreadsMap;
    private boolean threadContentionMonitoringEnabled;
    private boolean hotMethodProfilingEnabled;
	private int topThreadCount;
    private int blockedThreadsCount;
    private HotMethods hotMethods;

    private static final Logger logger = Logger.getLogger(ThreadUsage.class.getName());

    public ThreadUsage( MBeanServerConnection mbsc, Map<Integer, TopThread> topThreadsMap ) throws IOException {
        final OperatingSystemMXBean osbean =
                ManagementFactory.getPlatformMXBean( mbsc, OperatingSystemMXBean.class );
        this.numberOfProcessors = osbean.getAvailableProcessors();
        this.setTopThreadsMap(topThreadsMap);
        this.topThreadCount = topThreadsMap.size();
        this.threadMXBean = ManagementFactory.getPlatformMXBean( mbsc, ThreadMXBean.class );
    }

    public ThreadUsage( MBeanServerConnection mbsc, Map<Integer, TopThread> topThreadsMap, Map<Integer, BlockedThread> blockedThreadsMap ) throws IOException {

        this(mbsc, topThreadsMap);

        if ( threadMXBean.isThreadContentionMonitoringSupported() ) {
            // enable thread contention monitoring
            threadMXBean.setThreadContentionMonitoringEnabled(true);
            this.threadContentionMonitoringEnabled = true;
            logger.fine("Thread Contention Monitoring Enabled: " + threadMXBean.isThreadContentionMonitoringEnabled());
            this.blockedThreadsCount = blockedThreadsMap.size();
        }
        else {
            logger.warning("Thread contention monitoring not supported by this JVM.");
        }

        this.setBlockedThreadsMap(blockedThreadsMap);

    }

    public ThreadUsage( MBeanServerConnection mbsc, Map<Integer, TopThread> topThreadsMap, HotMethods hotMethods, long hotMethodPollInterval ) throws IOException {

        this(mbsc, topThreadsMap);
        this.hotMethodProfilingEnabled = true;
        // store hotMethods
        this.hotMethods = hotMethods;
        // init hot method tracker
        initHotMethodTracker( topThreadsMap, hotMethods, hotMethodPollInterval );

    }

    public ThreadUsage(MBeanServerConnection mbsc, Map<Integer, TopThread> topThreadsMap, Map<Integer, BlockedThread> blockedThreadsMap, HotMethods hotMethods, long hotMethodPollInterval) throws IOException {

        this(mbsc, topThreadsMap, blockedThreadsMap);
        this.hotMethodProfilingEnabled = true;
        // store hotMethods
        this.hotMethods = hotMethods;
        // init hot method tracker
        initHotMethodTracker( topThreadsMap, hotMethods, hotMethodPollInterval );

    }

    /** Update thread usage stats. */
    public synchronized void update( ) {
        final long[] ids = threadMXBean.getAllThreadIds( );
        final java.lang.management.ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(ids);
        double totalCpuTime = 0;
        double totalUserCpuTime = 0;
        ThreadTimeMap cpuTimeMap = new ThreadTimeMap();
        ThreadTimeMap blockedTimeMap = new ThreadTimeMap();
        long threadCount = 0;
        long runnableThreadCount = 0;
        long blockedThreadCount = 0;
        long waitingThreadCount = 0;
        long timedWaitingThreadCount = 0;
        // reset threadHistory activity tracking
        resetActivityTracker();
        for ( java.lang.management.ThreadInfo jmxThreadInfo : threadInfos ) {

            final long id;
            final String name;
            State state;
            if (jmxThreadInfo != null) {
                id = jmxThreadInfo.getThreadId();
                name = jmxThreadInfo.getThreadName();
                state = jmxThreadInfo.getThreadState();
            }
            else {
                continue;   // Assume thread died
            }
            final long threadCpuTime = threadMXBean.getThreadCpuTime(id);
            final long threadUserTime = threadMXBean.getThreadUserTime(id);
            final long systemTime = System.currentTimeMillis();
            if ( threadCpuTime == -1 || threadUserTime == -1 ) {
            	continue;   // Thread died
            }
            long threadBlockedTime = 0;
            if (threadContentionMonitoringEnabled) {
                if (jmxThreadInfo != null) {
                    threadBlockedTime = jmxThreadInfo.getBlockedTime();
                }
            }

            ThreadInfo threadInfo = threadHistory.get( id );
            if ( threadInfo == null ) {

                // create new ThreadInfo object
                threadInfo = new ThreadInfo( );
                threadInfo.id = id;
                threadInfo.name = name;
                threadInfo.state = state;
                threadInfo.active = true;
                threadInfo.interval = 0;
                threadInfo.startTime     = systemTime;
                threadInfo.startCpuTime  = threadCpuTime;
                threadInfo.startUserTime = threadUserTime;
                threadInfo.endTime       = systemTime;
                threadInfo.endCpuTime    = threadCpuTime;
                threadInfo.endUserTime   = threadUserTime;
                threadInfo.lastBlockedTime = threadBlockedTime;
                threadInfo.intervalBlockedTime = 0;
                threadInfo.cpuUsage = 0;
                cpuTimeMap.put((long) 0, id);
                threadInfo.userCpuUsage = 0;
                threadHistory.put(id, threadInfo);

            } else {

                // update existing ThreadInfo object
                threadInfo.state = state;
                threadInfo.active = true;
                threadInfo.endTime = systemTime;
                threadInfo.endCpuTime  = threadCpuTime;
                threadInfo.endUserTime = threadUserTime;
                long timeDiff = threadInfo.endTime - threadInfo.startTime;
                threadInfo.interval = timeDiff;
                // calculate CPU time in nano secs consumed during interval
                long cpuTimeDiff = threadInfo.endCpuTime - threadInfo.startCpuTime;
                // calculate thread CPU usage as percentage of wall clock time
                double cpuTimeDiffMillis = cpuTimeDiff / 1000000;
                double threadCpuUsage = ( cpuTimeDiffMillis / timeDiff ) * 100;
                threadInfo.cpuUsage = threadCpuUsage;
                // calculate CPU user time in nano secs consumed during interval
                long userTimeDiff = threadInfo.endUserTime - threadInfo.startUserTime;
                // add threadCpuUsage to overall cpuTime
                totalCpuTime += threadCpuUsage;
                // add cpuTimeDiff to cpuTimeMap
                cpuTimeMap.put(cpuTimeDiff, id);
                // calculate thread user CPU usage as percentage of wall clock time
                double userTimeDiffMillis = userTimeDiff / 1000000;
                double threadUserCpuUsage = ( userTimeDiffMillis / timeDiff ) * 100;
                threadInfo.userCpuUsage = threadUserCpuUsage;
                // add threadUserCpuUsage to overall userCpuTime
                totalUserCpuTime += threadUserCpuUsage;
                // reset start timers for next iteration
                threadInfo.startTime = systemTime;
                threadInfo.startCpuTime  = threadCpuTime;
                threadInfo.startUserTime = threadUserTime;
                if (threadContentionMonitoringEnabled) {
                    // calculate interval blocked time
                    threadInfo.intervalBlockedTime = threadBlockedTime - threadInfo.lastBlockedTime;
                    // add intervalBlockedTime to blockedTimeMap
                    blockedTimeMap.put(threadInfo.intervalBlockedTime, id);
                    // persist this threadBlockedTime
                    threadInfo.lastBlockedTime = threadBlockedTime;
                }
            }

            // check thread state
            if (state == null) {
                // assume thread has terminated
                state = Thread.State.TERMINATED;
            }

            // increment threadCount
            threadCount++;

            // increment state specific thread counters
            switch (state) {

                case NEW:
                    break;
                case RUNNABLE:
                    runnableThreadCount++;
                    break;
                case BLOCKED:
                    blockedThreadCount++;
                    break;
                case WAITING:
                    waitingThreadCount++;
                    break;
                case TIMED_WAITING:
                    timedWaitingThreadCount++;
                    break;
                case TERMINATED:
                    break;
            }

        }

        // clean up threadHistory
        cleanUpThreadHistory();
        // update cpuUsage
        this.cpuUsage = totalCpuTime / numberOfProcessors;
        logger.fine("CPU Usage = " + this.cpuUsage);
        // update userCpuUsage
        this.userCpuUsage = totalUserCpuTime / numberOfProcessors;
        logger.fine("User CPU Usage = " + this.userCpuUsage);
        // update sysCpuUsage
        this.sysCpuUsage = ( totalCpuTime - totalUserCpuTime) / numberOfProcessors;
        logger.fine("System CPU Usage = " + this.sysCpuUsage);
        // update cpuTimeMap
        this.cpuTimeMap = cpuTimeMap;
        //logger.fine("cpuTimeMap: " + cpuTimeMap.toString());
        if (threadContentionMonitoringEnabled) {
            // update blockedTimeMap
            this.blockedTimeMap = blockedTimeMap;
            // update blockedThreadsMap
            updateBlockedThreads();
        }
        // update threadCount
        this.setThreadCount(threadCount);
        // update runnableThreadCount
        this.setRunnableThreadCount(runnableThreadCount);
        // update blockedThreadCount
        this.setBlockedThreadCount(blockedThreadCount);
        // update waitingThreadCount
        this.setWaitingThreadCount(waitingThreadCount);
        // update timedWaitingThreadCount
        this.setTimedWaitingThreadCount(timedWaitingThreadCount);
        // update topThreadsMap
        updateTopThreads();
        if (hotMethodProfilingEnabled) {
            // update hot methods
            hotMethods.update();
        }
    }

    private synchronized void resetActivityTracker( ) {

        // reset activity tracker within threadHistory ThreadInfo to false
        for (Map.Entry<Long,ThreadInfo> entry : threadHistory.entrySet()) {
            entry.getValue().active = false;
        }
    }

    private synchronized void cleanUpThreadHistory( ) {

        // remove inactive threads from threadHistory
        for(Iterator<Map.Entry<Long, ThreadInfo>> it = threadHistory.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Long, ThreadInfo> entry = it.next();
            if (! entry.getValue().active ) {
                it.remove();
            }
        }

    }

    /** Get CPU usage. */
    public double getCpuUsage( ) {
    	return this.cpuUsage;
    }
    
    /** Get User CPU usage. */
    public double getUserCpuUsage( ) {
    	return this.userCpuUsage;
    }
    
    /** Get System CPU usage. */
    public double getSysCpuUsage( ) {
    	return this.sysCpuUsage;
    }
    
    public void updateTopThreads( ) {
    	
    	Object[] threadCpuTimeArray = cpuTimeMap.descendingKeySet().toArray();
    	int threadCounter = 1;
    	String threadName;
    	List<Long> threadIds;
    	State threadState;
    	double threadCpuUsage;
    	java.lang.management.ThreadInfo threadInfo;
    	int topThreadLimit;
    	// handle situation where threadCount less than topThreadCount
    	if ( threadCount < topThreadCount ) {
    		topThreadLimit = (int) threadCount;
    	}
    	else {
    		topThreadLimit = topThreadCount;
    	}
    	threadLimitLoop:
    	for (int i=0; i<topThreadLimit; i++) {
    		// get threadIds
    		threadIds = cpuTimeMap.get( (Long) threadCpuTimeArray[i] );
    		for ( long threadId : threadIds ) {
    			
    			// get cpuUsage
        		threadCpuUsage = threadHistory.get( threadId ).cpuUsage;
      			// get threadName
      			threadName = threadHistory.get( threadId ).name;
       			// get threadState
       			threadState = threadHistory.get( threadId ).state;
        		// retrieve threadCounter TopThread
        		TopThread topThreadsMBean = topThreadsMap.get(threadCounter);
        		// update topThreadsMBean attributes
        		topThreadsMBean.setThreadName(threadName);
        		topThreadsMBean.setThreadId(threadId);
        		topThreadsMBean.setThreadState(threadState);
        		topThreadsMBean.setThreadCpuUsage(threadCpuUsage);
                //topThreadsMBean.setThreadInfo(threadInfo);
        		logger.fine("threadCounter: " + threadCounter + ", threadCpuUsage: " + threadCpuUsage + ", threadId: " + threadId);
        		// check threadCount
        		if ( threadCounter == topThreadLimit ) {
        			// topThreadCount reached - break out of loop
        			break threadLimitLoop;
        		}
        		// increment threadCounter
        		threadCounter++;
    		}
    		
    	}
    	
    }

    public void updateBlockedThreads( ) {

        Object[] threadBlockedTimeArray = blockedTimeMap.descendingKeySet().toArray();
        int threadCounter = 1;
        String threadName;
        List<Long> threadIds;
        State threadState;
        long threadBlockedTime;
        java.lang.management.ThreadInfo threadInfo;
        int blockedThreadLimit;
        // handle situation where threadCount less than blockedThreadsCount
        if ( blockedThreadCount < blockedThreadsCount ) {
            blockedThreadLimit = (int) blockedThreadCount;
        }
        else {
            blockedThreadLimit = blockedThreadsCount;
        }
        threadLimitLoop:
        for (int i=0; i<blockedThreadLimit; i++) {
            // get threadIds
            threadIds = blockedTimeMap.get( (Long) threadBlockedTimeArray[i] );
            for ( long threadId : threadIds ) {

                // get blockedTime
                threadBlockedTime = threadHistory.get( threadId ).intervalBlockedTime;
                if (threadBlockedTime > 0) {

                    /*
                    /  this thread has registered some blocked time during this interval
                    */

                    // get interval time difference
                    long timeDiff = threadHistory.get( threadId ).interval;
                    // calculate percentage of interval time spent blocked
                    double threadBlockedPercentage = 0.0;
                    if (timeDiff > 0) {
                        threadBlockedPercentage = ( (double) threadBlockedTime / (double) timeDiff ) * 100;
                    }
                    // get threadName
                    threadName = threadHistory.get( threadId ).name;
                    // get threadState
                    threadState = threadHistory.get( threadId ).state;
                    // retrieve threadCounter BlockedThread
                    BlockedThread blockedThreadMBean = blockedThreadsMap.get(threadCounter);
                    // and update blockedThreadMBean attributes
                    blockedThreadMBean.setThreadName(threadName);
                    blockedThreadMBean.setThreadId(threadId);
                    blockedThreadMBean.setThreadState(threadState);
                    blockedThreadMBean.setThreadBlockedTime(threadBlockedTime);
                    blockedThreadMBean.setThreadBlockedPercentage(threadBlockedPercentage);
                }
                else {

                    /*
                    /  this thread hasn't registered any blocked time during this interval
                    */

                    // retrieve threadCounter BlockedThread
                    BlockedThread blockedThreadMBean = blockedThreadsMap.get(threadCounter);
                    // and update blockedThreadMBean attributes with null values
                    blockedThreadMBean.setThreadName(null);
                    blockedThreadMBean.setThreadId(0);
                    blockedThreadMBean.setThreadState(null);
                    blockedThreadMBean.setThreadBlockedTime(0);
                    blockedThreadMBean.setThreadBlockedPercentage(0.0);
                }

                logger.fine("threadCounter: " + threadCounter + ", threadBlockedTime: " + threadBlockedTime + ", threadId: " + threadId);
                // check threadCount
                if ( threadCounter == blockedThreadLimit ) {
                    // blockedThreadsCount reached - break out of loop
                    break threadLimitLoop;
                }
                // increment threadCounter
                threadCounter++;
            }

        }

    }

    /** Get total CPU time so far in nanoseconds. */
    public long getTotalCpuTime( ) {
        final Collection<ThreadInfo> hist = threadHistory.values( );
        long time = 0L;
        for ( ThreadInfo threadInfo : hist )
            time += threadInfo.endCpuTime - threadInfo.startCpuTime;
        return time;
    }

    /** Get total user time so far in nanoseconds. */
    public long getTotalUserTime( ) {
        final Collection<ThreadInfo> hist = threadHistory.values( );
        long time = 0L;
        for ( ThreadInfo threadInfo : hist )
            time += threadInfo.endUserTime - threadInfo.startUserTime;
        return time;
    }

    /** Get total system time so far in nanoseconds. */
    public long getTotalSystemTime( ) {
        return getTotalCpuTime( ) - getTotalUserTime( );
    }

	public long getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(long threadCount) {
		this.threadCount = threadCount;
	}

	public void setCpuUsage(double cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public void setUserCpuUsage(double userCpuUsage) {
		this.userCpuUsage = userCpuUsage;
	}

	public void setSysCpuUsage(double sysCpuUsage) {
		this.sysCpuUsage = sysCpuUsage;
	}

	public Map<Integer, TopThread> getTopThreadsMap() {
		return topThreadsMap;
	}

	private void setTopThreadsMap(Map<Integer, TopThread> topThreadsMap) {
		this.topThreadsMap = topThreadsMap;
	}

    public Map<Integer, BlockedThread> getBlockedThreadsMap() {
        return blockedThreadsMap;
    }

    public void setBlockedThreadsMap(Map<Integer, BlockedThread> blockedThreadsMap) {
        this.blockedThreadsMap = blockedThreadsMap;
    }

    public long getRunnableThreadCount() {
        return runnableThreadCount;
    }

    public void setRunnableThreadCount(long runnableThreadCount) {
        this.runnableThreadCount = runnableThreadCount;
    }

    public long getBlockedThreadCount() {
        return blockedThreadCount;
    }

    public void setBlockedThreadCount(long blockedThreadCount) {
        this.blockedThreadCount = blockedThreadCount;
    }

    public long getWaitingThreadCount() {
        return waitingThreadCount;
    }

    public void setWaitingThreadCount(long waitingThreadCount) {
        this.waitingThreadCount = waitingThreadCount;
    }

    public long getTimedWaitingThreadCount() {
        return timedWaitingThreadCount;
    }

    public void setTimedWaitingThreadCount(long timedWaitingThreadCount) {
        this.timedWaitingThreadCount = timedWaitingThreadCount;
    }

    private void initHotMethodTracker (Map<Integer, TopThread> topThreadsMap, HotMethods hotMethods, long pollInterval) {

        // create new TimerTask to run hot method tracker
    	TimerTask hotMethodTracker = new HotMethodTracker( topThreadsMap, hotMethods );
    	// create new Timer to schedule hot method tracker
    	Timer timer = new Timer("Top4J Method Profiler", true);
    	// run hot method tracker at fixed interval
    	timer.scheduleAtFixedRate(hotMethodTracker, 0, pollInterval);

    }

    public HotMethods getHotMethods( ) {
        return this.hotMethods;
    }
}
