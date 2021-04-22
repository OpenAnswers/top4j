/*
 * Copyright (c) 2019 Open Answers Ltd. https://www.openanswers.co.uk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.top4j.javaagent.mbeans.jvm.threads;

import io.top4j.javaagent.config.Configurator;

import javax.management.*;
import java.io.IOException;
import java.lang.Thread.State;
import java.lang.management.*;
import java.util.*;
import java.util.logging.*;

public class ThreadUsage {

    private static class ThreadInfo {
        long id;
        String name;
        State state;
        boolean active = true;
        long interval = 0;
        long startTime = 0;
        long startCpuTime = 0;
        long startUserTime = 0;
        long endTime = 0;
        long endCpuTime = 0;
        long endUserTime = 0;
        long lastBlockedTime = 0;
        long intervalBlockedTime = 0;
        double cpuUsage = 0.0;
        double userCpuUsage = 0.0;

        private ThreadInfo(long id, long startTime) {
            this.id = id;
            this.startTime = startTime;
        }
    }

    private int numberOfProcessors;
    private final Map<Long, ThreadInfo> threadHistory =
            new HashMap<>();
    volatile private double cpuUsage;
    private final Map<Long, ThreadInfo> threadHistoryCache =
            new HashMap<>();
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
    private final MBeanServerConnection mbeanServer;
    private Map<Integer, TopThread> topThreadsMap;
    private Map<Integer, BlockedThread> blockedThreadsMap;
    private boolean threadContentionMonitoringEnabled;
    private boolean hotMethodProfilingEnabled;
    private int topThreadCount;
    private int blockedThreadsCount;
    private HotMethods hotMethods;
    private boolean threadCacheEnabled;
    private int threadCacheSize;
    private int topThreadCacheSize;
    private int blockedThreadCacheSize;
    private int threadCacheTTL;
    private long lastThreadCacheRefreshTime = 0;
    private long newThreadStartTime;
    private int internalThreadScanLimit = 0;
    private Long[] internalThreadIds;
    private long lastSystemTime;
    private long lastProcessCpuTime;
    volatile private double processCpuUsage = -1; // default - not available

    private static final Logger LOGGER = Logger.getLogger(ThreadUsage.class.getName());

    public ThreadUsage(Configurator config, Map<Integer, TopThread> topThreadsMap) throws IOException {
        this.mbeanServer = config.getMBeanServerConnection();
        OperatingSystemMXBean osbean =
                ManagementFactory.getPlatformMXBean(config.getMBeanServerConnection(), OperatingSystemMXBean.class);
        this.numberOfProcessors = osbean.getAvailableProcessors();
        this.setTopThreadsMap(topThreadsMap);
        this.topThreadCount = topThreadsMap.size();
        this.threadMXBean = ManagementFactory.getPlatformMXBean(config.getMBeanServerConnection(), ThreadMXBean.class);
        final RuntimeMXBean runtimeMXBean = ManagementFactory.getPlatformMXBean(config.getMBeanServerConnection(), RuntimeMXBean.class);
        long jvmStartUpTime = System.nanoTime() - (runtimeMXBean.getUptime() * 1000000);
        this.newThreadStartTime = jvmStartUpTime;
        this.internalThreadScanLimit = Integer.parseInt(config.get("thread.internal.scan.limit"));
        this.internalThreadIds = getInternalThreadIds();
        // next two for process cpu calculation:
        this.lastSystemTime = jvmStartUpTime;
        // we want to match similar calculation for thread cpu; first time through this is mean since jvm started, so if process cpu is available, we set to zero, while
        // -1 means it is not available. This avoids anomalies between threads and process the first time the cpu is displayed.
        if (getProcessCpuTime() > 0) {
            this.lastProcessCpuTime = 0;
            this.processCpuUsage = 0;
        }

        if (config.isThreadUsageCacheEnabled()) {
            // enable thread usage cache
            this.threadCacheEnabled = true;
            // set threadCacheSize
            this.threadCacheSize = Integer.parseInt(config.get("thread.usage.cache.size"));
            // set topThreadCacheSize to threadCacheSize by default (if thread contention monitoring is enabled we'll split the threadCache between top and blocked threads later on)
            this.topThreadCacheSize = threadCacheSize;
            // set threadCacheTTL
            this.threadCacheTTL = Integer.parseInt(config.get("thread.usage.cache.ttl"));
            // warm up the threadCache via this.update()
            LOGGER.info("Warming up thread usage cache....");
            this.update();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LOGGER.warning("Warm up thread usage cache process interrupted due to: " + e.getMessage());
            }
            // reset lastThreadCacheRefreshTime and re-run this.update()
            lastThreadCacheRefreshTime = 0;
            this.update();
        } else {
            // disable thread usage cache
            this.threadCacheEnabled = false;
        }
    }

    public ThreadUsage(Configurator config, Map<Integer, TopThread> topThreadsMap, Map<Integer, BlockedThread> blockedThreadsMap) throws IOException {

        this(config, topThreadsMap);

        if (threadMXBean.isThreadContentionMonitoringSupported()) {
            // enable thread contention monitoring
            threadMXBean.setThreadContentionMonitoringEnabled(true);
            this.threadContentionMonitoringEnabled = true;
            LOGGER.fine("Thread Contention Monitoring Enabled: " + threadMXBean.isThreadContentionMonitoringEnabled());
            this.blockedThreadsCount = blockedThreadsMap.size();
            if (config.isThreadUsageCacheEnabled()) {
                // split threadCache between top threads and blocked threads in a ratio of 4:1
                this.topThreadCacheSize = (int) (threadCacheSize * 0.8);
                this.blockedThreadCacheSize = (int) (threadCacheSize * 0.2);
            }

        } else {
            LOGGER.warning("Thread contention monitoring not supported by this JVM.");
        }

        this.setBlockedThreadsMap(blockedThreadsMap);

    }

    public ThreadUsage(Configurator config, Map<Integer, TopThread> topThreadsMap, HotMethods hotMethods, long hotMethodPollInterval) throws IOException {

        this(config, topThreadsMap);
        this.hotMethodProfilingEnabled = true;
        // store hotMethods
        this.hotMethods = hotMethods;
        // init hot method tracker
        initHotMethodTracker(topThreadsMap, hotMethods, hotMethodPollInterval);

    }

    public ThreadUsage(Configurator config, Map<Integer, TopThread> topThreadsMap, Map<Integer, BlockedThread> blockedThreadsMap, HotMethods hotMethods, long hotMethodPollInterval) throws IOException {

        this(config, topThreadsMap, blockedThreadsMap);
        this.hotMethodProfilingEnabled = true;
        // store hotMethods
        this.hotMethods = hotMethods;
        // init hot method tracker
        initHotMethodTracker(topThreadsMap, hotMethods, hotMethodPollInterval);

    }

    /**
     * Update thread usage stats.
     */
    public synchronized void update() {

        if (threadCacheEnabled && (threadCount > threadCacheSize)) {
            LOGGER.fine("Thread usage cache enabled....");
            // get current time in millis
            final long currentTime = System.currentTimeMillis();
            // thread cache enabled - check if cache TTL has expired before updating thread usage history
            if (currentTime > (lastThreadCacheRefreshTime + threadCacheTTL)) {
                // thread cache TTL has expired - refresh threadHistory with full set of thread IDs
                refreshThreadHistory();
                // we've got a full set of threads - update full threadHistory and global counters
                update(threadHistory, true);
                // refresh threadCache ready for next update
                refreshThreadHistoryCache();
                // update lastThreadCacheRefreshTime
                lastThreadCacheRefreshTime = currentTime;
            } else {
                // thread cache still fresh - update threadHistoryCache but don't update global counters
                update(threadHistoryCache, false);
            }
        } else {
            // thread cache disabled - refresh threadHistory with full set of thread IDs
            refreshThreadHistory();
            // we've got a full set of threads - update full threadHistory and global counters
            update(threadHistory, true);
        }

        // update topThreadsMap
        updateTopThreads();

        if (threadContentionMonitoringEnabled) {
            // update blockedThreadsMap
            updateBlockedThreads();
        }

        if (hotMethodProfilingEnabled) {
            // update hot methods
            hotMethods.update();
        }

    }

    /**
     * Update thread usage stats for threadHistory threads.
     */
    private synchronized void update(Map<Long, ThreadInfo> threadHistory, boolean updateGlobalCounters) {

        double totalCpuTime = 0;
        double totalUserCpuTime = 0;
        ThreadTimeMap cpuTimeMap = new ThreadTimeMap();
        ThreadTimeMap blockedTimeMap = new ThreadTimeMap();
        long threadCount = 0;
        long runnableThreadCount = 0;
        long blockedThreadCount = 0;
        long waitingThreadCount = 0;
        long timedWaitingThreadCount = 0;
        // iterate over ThreadUsage.ThreadInfo objects within threadHistory map and update with latest JMX ThreadMXBean ThreadInfo
        for (Map.Entry<Long, ThreadInfo> entry : threadHistory.entrySet()) {

            // retrieve ThreadInfo object
            ThreadInfo threadInfo = entry.getValue();
            final String name;
            State state;
            long id = threadInfo.id;
            // get JMX ThreadInfo for thread ID from ThreadMXBean
            java.lang.management.ThreadInfo jmxThreadInfo = threadMXBean.getThreadInfo(id);
            if (jmxThreadInfo != null) {
                id = jmxThreadInfo.getThreadId();
                name = jmxThreadInfo.getThreadName();
                state = jmxThreadInfo.getThreadState();
            } else {
                // assume thread died
                threadInfo.active = false;
                threadInfo.state = State.TERMINATED;
                continue;
            }
            final long threadCpuTime = threadMXBean.getThreadCpuTime(id);
            final long threadUserTime = threadMXBean.getThreadUserTime(id);
            final long systemTime = System.nanoTime();
            if (threadCpuTime == -1 || threadUserTime == -1) {
                // assume thread died
                threadInfo.active = false;
                threadInfo.state = State.TERMINATED;
                continue;
            }
            long threadBlockedTime = 0;
            if (threadContentionMonitoringEnabled) {
                threadBlockedTime = jmxThreadInfo.getBlockedTime();
            }

            // update ThreadInfo object
            threadInfo.name = name;
            threadInfo.state = state;
            threadInfo.active = true;
            threadInfo.endTime = systemTime;
            threadInfo.endCpuTime = threadCpuTime;
            threadInfo.endUserTime = threadUserTime;
            long timeDiff = threadInfo.endTime - threadInfo.startTime;
            threadInfo.interval = timeDiff;
            // calculate CPU time in nano secs consumed during interval
            long cpuTimeDiff = threadInfo.endCpuTime - threadInfo.startCpuTime;
            // calculate thread CPU usage as percentage of wall clock time
            double threadCpuUsage = calculateCpuUsage(cpuTimeDiff, timeDiff);
            threadInfo.cpuUsage = threadCpuUsage;
            // add threadCpuUsage to overall cpuTime
            totalCpuTime += threadCpuUsage;
            // add cpuTimeDiff to cpuTimeMap
            cpuTimeMap.put(cpuTimeDiff, id);
            // calculate CPU user time in nano secs consumed during interval
            long userTimeDiff = threadInfo.endUserTime - threadInfo.startUserTime;
            // calculate thread user CPU usage as percentage of wall clock time
            double threadUserCpuUsage = calculateCpuUsage(userTimeDiff, timeDiff);
            threadInfo.userCpuUsage = threadUserCpuUsage;
            // add threadUserCpuUsage to overall userCpuTime
            totalUserCpuTime += threadUserCpuUsage;
            // reset start timers for next iteration
            threadInfo.startTime = systemTime;
            threadInfo.startCpuTime = threadCpuTime;
            threadInfo.startUserTime = threadUserTime;
            if (threadContentionMonitoringEnabled) {
                // calculate interval blocked time
                threadInfo.intervalBlockedTime = threadBlockedTime - threadInfo.lastBlockedTime;
                // add intervalBlockedTime to blockedTimeMap
                blockedTimeMap.put(threadInfo.intervalBlockedTime, id);
                // persist this threadBlockedTime
                threadInfo.lastBlockedTime = threadBlockedTime;
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
                default:
                    break;
            }
        }

        // update cpuTimeMap
        this.cpuTimeMap = cpuTimeMap;

        if (threadContentionMonitoringEnabled) {
            // update blockedTimeMap
            this.blockedTimeMap = blockedTimeMap;
        }

        if (updateGlobalCounters) {

            // do this here to keep (semi-)synchronised with thread cpu calculation
            updateProcessCpuTime();

            // update cpuUsage
            this.cpuUsage = totalCpuTime / numberOfProcessors;
            LOGGER.fine("CPU Usage = " + this.cpuUsage);
            // update userCpuUsage
            this.userCpuUsage = totalUserCpuTime / numberOfProcessors;
            LOGGER.fine("User CPU Usage = " + this.userCpuUsage);
            // update sysCpuUsage
            this.sysCpuUsage = (totalCpuTime - totalUserCpuTime) / numberOfProcessors;
            LOGGER.fine("System CPU Usage = " + this.sysCpuUsage);
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
        }

    }

    private synchronized void resetActivityTracker() {

        // reset activity tracker within threadHistory ThreadInfo to false
        for (Map.Entry<Long, ThreadInfo> entry : threadHistory.entrySet()) {
            entry.getValue().active = false;
        }
    }

    private synchronized void cleanUpThreadHistory() {

        // remove inactive threads from threadHistory
        for (Iterator<Map.Entry<Long, ThreadInfo>> it = threadHistory.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Long, ThreadInfo> entry = it.next();
            if (!entry.getValue().active) {
                it.remove();
            }
        }

    }

    /**
     * Refresh global threadHistory map with with complete list of running threads via JMX ThreadMXBean.getAllThreadIds() method
     */

    private synchronized void refreshThreadHistory() {

        // reset threadHistory activity tracking
        resetActivityTracker();

        // refresh threadHistory with complete list of running threads via ThreadMXBean
        for (long id : threadMXBean.getAllThreadIds()) {
            // retrieve threadInfo object from threadHistory map
            ThreadInfo threadInfo = threadHistory.get(id);
            if (threadInfo == null) {
                // create new ThreadInfo object
                threadInfo = new ThreadInfo(id, newThreadStartTime);
                // add new ThreadInfo object to threadHistory
                threadHistory.put(id, threadInfo);
            } else {
                threadInfo.active = true;
            }
        }

        // Check for additional system threads which are not returned by ThreadMXBean.getAllThreadIds;
        // see getInternalThreadIds().
        for (long id : this.internalThreadIds) {
            ThreadInfo threadInfo = threadHistory.get(id);
            if (threadInfo == null) {
                // create new ThreadInfo object
                threadInfo = new ThreadInfo(id, newThreadStartTime);
                // add new ThreadInfo object to threadHistory
                threadHistory.put(id, threadInfo);
            } else {
                threadInfo.active = true;
            }
        }

        // clean up threadHistory
        cleanUpThreadHistory();

    }

    private Long[] getInternalThreadIds() {
        // if internalThreadScanLimit>0, check for additional system threads which are known to ThreadMXBean
        // but are not returned by ThreadMXBean.getAllThreadIds. We only do this once.
        Map seen = new HashMap<Long, Object>();
        for (long id : threadMXBean.getAllThreadIds()) {
            seen.put(id, null);  // record normal threads
        }
        List<Long> internalIds = new ArrayList<Long>();
        // now scan for unseen ids in range [1,internalThreadScanLimit] inclusive.
        for (long id = 1; id <= this.internalThreadScanLimit; id++) {
            if (seen.containsKey(id))
                continue;
            if (threadMXBean.getThreadInfo(id) != null)
                internalIds.add(id);
        }
        return internalIds.toArray(new Long[internalIds.size()]);
    }

    /**
     * Get CPU usage.
     * @return the CPU usage
     */
    public double getCpuUsage() {
        return this.cpuUsage;
    }

    /**
     * Get User CPU usage.
     * @return the user CPU usage
     */
    public double getUserCpuUsage() {
        return this.userCpuUsage;
    }

    /**
     * Get System CPU usage.
     * @return the system CPU usage
     */
    public double getSysCpuUsage() {
        return this.sysCpuUsage;
    }

    /**
     * Get Process CPU usage (&lt;0 if not available).
     * @return the Java process CPU usage
     */
    public double getProcessCpuUsage() {
        return this.processCpuUsage;
    }

    public synchronized void updateTopThreads() {

        Object[] threadCpuTimeArray = cpuTimeMap.descendingKeySet().toArray();
        int threadCounter = 1;
        String threadName;
        List<Long> threadIds;
        State threadState;
        double threadCpuUsage;
        int topThreadLimit;
        // handle situation where threadCount less than topThreadCount
        if (threadCount < topThreadCount) {
            topThreadLimit = (int) threadCount;
        } else {
            topThreadLimit = topThreadCount;
        }
        threadLimitLoop:
        for (int i = 0; i < topThreadLimit; i++) {
            // get threadIds
            threadIds = cpuTimeMap.get((Long) threadCpuTimeArray[i]);
            for (long threadId : threadIds) {

                // get cpuUsage
                threadCpuUsage = threadHistory.get(threadId).cpuUsage;
                // get threadName
                threadName = threadHistory.get(threadId).name;
                // get threadState
                threadState = threadHistory.get(threadId).state;
                // retrieve threadCounter TopThread
                TopThread topThreadsMBean = topThreadsMap.get(threadCounter);
                // update topThreadsMBean attributes
                topThreadsMBean.setThreadName(threadName);
                topThreadsMBean.setThreadId(threadId);
                topThreadsMBean.setThreadState(threadState);
                topThreadsMBean.setThreadCpuUsage(threadCpuUsage);
                LOGGER.fine("threadCounter: " + threadCounter + ", threadCpuUsage: " + threadCpuUsage + ", threadId: " + threadId);
                // check threadCount
                if (threadCounter == topThreadLimit) {
                    // topThreadCount reached - break out of loop
                    break threadLimitLoop;
                }
                // increment threadCounter
                threadCounter++;
            }

        }

    }

    public synchronized void updateBlockedThreads() {

        Object[] threadBlockedTimeArray = blockedTimeMap.descendingKeySet().toArray();
        int threadCounter = 1;
        String threadName;
        List<Long> threadIds;
        State threadState;
        long threadBlockedTime;
        int blockedThreadLimit;
        // handle situation where threadCount less than blockedThreadsCount
        if (threadCount < blockedThreadsCount) {
            blockedThreadLimit = (int) threadCount;
        } else {
            blockedThreadLimit = blockedThreadsCount;
        }
        threadLimitLoop:
        for (int i = 0; i < blockedThreadLimit; i++) {
            // get threadIds
            threadIds = blockedTimeMap.get((Long) threadBlockedTimeArray[i]);
            for (long threadId : threadIds) {

                // get blockedTime
                threadBlockedTime = threadHistory.get(threadId).intervalBlockedTime;
                if (threadBlockedTime > 0) {

                    /*
                    /  this thread has registered some blocked time during this interval
                    */

                    // get interval time difference
                    long timeDiff = threadHistory.get(threadId).interval;
                    // calculate percentage of interval time spent blocked
                    double threadBlockedPercentage = 0.0;
                    if (timeDiff > 0) {
                        threadBlockedPercentage = ((((double) threadBlockedTime) * 1000000) / (double) timeDiff) * 100;
                    }
                    // get threadName
                    threadName = threadHistory.get(threadId).name;
                    // get threadState
                    threadState = threadHistory.get(threadId).state;
                    // retrieve threadCounter BlockedThread
                    BlockedThread blockedThreadMBean = blockedThreadsMap.get(threadCounter);
                    // and update blockedThreadMBean attributes
                    blockedThreadMBean.setThreadName(threadName);
                    blockedThreadMBean.setThreadId(threadId);
                    blockedThreadMBean.setThreadState(threadState);
                    blockedThreadMBean.setThreadBlockedTime(threadBlockedTime);
                    blockedThreadMBean.setThreadBlockedPercentage(threadBlockedPercentage);
                } else {

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

                LOGGER.fine("threadCounter: " + threadCounter + ", threadBlockedTime: " + threadBlockedTime + ", threadId: " + threadId);
                // check threadCount
                if (threadCounter == blockedThreadLimit) {
                    // blockedThreadsCount reached - break out of loop
                    break threadLimitLoop;
                }
                // increment threadCounter
                threadCounter++;
            }

        }

    }

    /**
     * Get total CPU time so far in nanoseconds.
     * @return the total CPU time in nanoseconds
     */
    public long getTotalCpuTime() {
        final Collection<ThreadInfo> hist = threadHistory.values();
        long time = 0L;
        for (ThreadInfo threadInfo : hist) {
            time += threadInfo.endCpuTime - threadInfo.startCpuTime;
        }
        return time;
    }

    /**
     * Get total user time so far in nanoseconds.
     * @return the total user time in nanoseconds
     */
    public long getTotalUserTime() {
        final Collection<ThreadInfo> hist = threadHistory.values();
        long time = 0L;
        for (ThreadInfo threadInfo : hist) {
            time += threadInfo.endUserTime - threadInfo.startUserTime;
        }
        return time;
    }

    /**
     * Get total system time so far in nanoseconds.
     * @return the total system time in nanoseconds
     */
    public long getTotalSystemTime() {
        return getTotalCpuTime() - getTotalUserTime();
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

    private void initHotMethodTracker(Map<Integer, TopThread> topThreadsMap, HotMethods hotMethods, long pollInterval) {

        // create new TimerTask to run hot method tracker
        TimerTask hotMethodTracker = new HotMethodTracker(topThreadsMap, hotMethods);
        // create new Timer to schedule hot method tracker
        Timer timer = new Timer("Top4J Method Profiler", true);
        // run hot method tracker at fixed interval
        timer.scheduleAtFixedRate(hotMethodTracker, 0, pollInterval);

    }

    public HotMethods getHotMethods() {
        return this.hotMethods;
    }

    /**
     * Refresh top thread cache (aka threadCache). The threadCache is an array of threadCacheSize thread IDs which have consumed the most CPU time since the last cache refresh.
     */
    private void refreshThreadHistoryCache() {

        LOGGER.fine("Refreshing thread history cache....");
        int topThreadLimit;
        int blockedThreadLimit;
        Set<Long> threadSet = new HashSet<>();
        // handle situation where threadCount less than threadCacheSize
        if (threadCount < threadCacheSize) {
            // we shouldn't get here but just in case.... set thread limits to a percentage of threadCount
            topThreadLimit = (int) (((int) threadCount) * 0.8);
            blockedThreadLimit = (int) (((int) threadCount) * 0.2);
        } else {
            topThreadLimit = topThreadCacheSize;
            blockedThreadLimit = blockedThreadCacheSize;
        }
        // add top threadCacheSize thread IDs from cpuTimeMap to threadSet
        threadSet.addAll(getTopThreads(cpuTimeMap, topThreadLimit));
        if (threadContentionMonitoringEnabled) {
            // add top threadCacheSize thread IDs from BlockedTimeMap to threadSet
            threadSet.addAll(getTopThreads(blockedTimeMap, blockedThreadLimit));
        }
        // clear existing threadHistoryCache
        threadHistoryCache.clear();
        // populate threadHistoryCache with threadSet thread IDs
        for (Long id : threadSet) {
            // retrieve threadInfo from global threadHistory map and add it to threadHistoryCache
            threadHistoryCache.put(id, threadHistory.get(id));
        }

    }

    /**
     * Get list of top threads from a ThreadTimeMap up to and including threadCount threads
     *
     * @param threadTimeMap A ThreadTimeMap, e.g. cpuTimeMap
     * @param threadLimit   The number of threads to include in the thread ID Set returned
     * @return Set<Long> A Set of thread Ids
     */
    private Set<Long> getTopThreads(ThreadTimeMap threadTimeMap, int threadLimit) {

        Object[] threadCpuTimeArray = threadTimeMap.descendingKeySet().toArray();
        int threadCounter = 1;
        List<Long> threadIds;
        Set<Long> threadSet = new HashSet<>();
        // add top thread IDs to threadSet
        topThreadLimitLoop:
        for (int i = 0; i < threadLimit; i++) {
            // get threadIds
            threadIds = threadTimeMap.get((Long) threadCpuTimeArray[i]);
            for (long threadId : threadIds) {

                LOGGER.fine("Adding thread: threadCounter: " + threadCounter + ", threadId: " + threadId + " to thread cache");
                // add threadId to threadSet
                threadSet.add(threadId);
                // check threadCount
                if (threadCounter == threadLimit) {
                    // topThreadCount reached - break out of loop
                    break topThreadLimitLoop;
                }
                // increment threadCounter
                threadCounter++;
            }
        }

        return threadSet;
    }

    /**
     * Calculate CPU usage
     */
    private double calculateCpuUsage(long cpuTimeDiff, long timeDiff) {
        if (timeDiff <= 0)
            return 0;

        // calculate thread CPU usage as percentage of wall clock time, aka timeDiff
        return ((double) cpuTimeDiff / (double) timeDiff) * 100;
    }


    private long getProcessCpuTime() {
        try {
            return (Long) this.mbeanServer.getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "ProcessCpuTime");
        } catch (Exception e) {
            return -1;
        }

    }

    private void updateProcessCpuTime() {
        long currentCpu = getProcessCpuTime();
        if (currentCpu < 0) {
            LOGGER.fine("process cpu time not available");
            return; // not available
        }
        long currentSystemTime = System.nanoTime();
        this.processCpuUsage = calculateCpuUsage((currentCpu - this.lastProcessCpuTime) / this.numberOfProcessors, currentSystemTime - lastSystemTime);
        LOGGER.fine("Process CPU Usage = " + this.processCpuUsage);
        this.lastSystemTime = currentSystemTime;
        this.lastProcessCpuTime = currentCpu;
    }


}
