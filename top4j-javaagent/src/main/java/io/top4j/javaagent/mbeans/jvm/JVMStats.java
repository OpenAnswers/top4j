package io.top4j.javaagent.mbeans.jvm;

import io.top4j.javaagent.config.Configurator;
import io.top4j.javaagent.config.Constants;
import io.top4j.javaagent.mbeans.StatsMXBean;
import io.top4j.javaagent.mbeans.agent.AgentStats;
import io.top4j.javaagent.mbeans.agent.AgentStatsMXBean;
import io.top4j.javaagent.mbeans.jvm.gc.GCStats;
import io.top4j.javaagent.mbeans.jvm.gc.GCStatsMXBean;
import io.top4j.javaagent.mbeans.jvm.heap.HeapStats;
import io.top4j.javaagent.mbeans.jvm.heap.HeapStatsMXBean;
import io.top4j.javaagent.mbeans.jvm.memory.MemoryStats;
import io.top4j.javaagent.mbeans.jvm.memory.MemoryStatsMXBean;
import io.top4j.javaagent.mbeans.jvm.threads.*;
import io.top4j.javaagent.mbeans.logger.StatsLogger;
import io.top4j.javaagent.mbeans.logger.StatsLoggerMXBean;
import io.top4j.javaagent.messaging.LoggerQueue;
import io.top4j.javaagent.profiler.CpuTime;
import io.top4j.javaagent.utils.MBeanHelper;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.management.JMX;
import javax.management.MBeanServer;

public class JVMStats implements JVMStatsMXBean { 
	
	private LoggerQueue loggerQueue;
	private String statsLoggerNotification = "jvm stats";
    private CpuTime cpuTime = new CpuTime();
    private double mBeanCpuTime;
    private List<StatsMXBean> jvmStatsMBeans = new ArrayList<>();
	private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private Configurator config;
	
	private static final Logger LOGGER = Logger.getLogger(JVMStats.class.getName());
	
	public JVMStats ( Configurator config, LoggerQueue loggerQueue ) {
		
		LOGGER.fine("Initialising JVM stats....");

        this.config = config;
		this.loggerQueue = loggerQueue;
		
		// initialise ThreadStats
		initThreadStats();
		
		// initialise MemoryStats
		initMemoryStats();

		// initialise HeapStats
		initHeapStats();

		// initialise GCStats
		initGCStats();
		
     	// initialise AgentStats
     	initAgentStats();

        if (config.isStatsLoggerEnabled()) {
            // initialise StatsLogger
            initStatsLogger();
        }

    }
	
	/** Update JVM stats. */
    public synchronized void update( ) {

        // initialise thread CPU timer
    	cpuTime.init();

    	LOGGER.fine("Updating JVM stats....");

        // update jvmStats
        for (StatsMXBean jvmStats : jvmStatsMBeans) {
            jvmStats.update();
        }

        // update JVM stats CPU time
        mBeanCpuTime = cpuTime.getMillis();
    	
    }

    @Override
    public void setMBeanCpuTime(double agentCpuTime) {
       this.mBeanCpuTime = agentCpuTime;
    }

    @Override
    public double getMBeanCpuTime() {
        return mBeanCpuTime;
    }

    public synchronized void log( ) {
    	
    	// send notification to loggerQueue
    	loggerQueue.send(statsLoggerNotification);
    }
    
    /**
     * Init Threads Stats MBean
     */
	private void initThreadStats( ) {

		// get configured top thread count
		int topThreadCount = Integer.parseInt(config.get("top.thread.count"));
		// get configured thread contention monitoring switch
		boolean threadContentionMonitoringEnabled = config.isThreadContentionMonitoringEnabled();
        // get configured blocked thread count
        int blockedThreadCount = Integer.parseInt(config.get("blocked.thread.count"));
		// get configured hot method profiling switch
		boolean hotMethodProfilingEnabled = config.isHotMethodProfilingEnabledEnabled();
        // get configured hot method count
        int hotMethodCount = Integer.parseInt(config.get("hot.method.count"));
        // get configured hot method polling interval
        long hotMethodPollInterval = Long.parseLong(config.get("hot.method.poll.frequency"));

		// instantiate Map of TopThread MBeans
		Map<Integer, TopThread> topThreadsMap = new HashMap<>();
		for (int rank = 1; rank <= topThreadCount; rank++) {

			// convert rank to String
			String ranking = String.valueOf(rank);

			// instantiate new MBeanHelper based on this type, statsType and rank
            MBeanHelper topThreadsMBeanHelper = null;
            try {
                topThreadsMBeanHelper = new MBeanHelper(
                        Constants.JVM_STATS_TYPE, Constants.TOP_THREAD_STATS_TYPE, ranking);
                // instantiate new TopThread MBean
                TopThread topThreadsBean = new TopThread( config.mBeanServerConnection );
                // add topThreadsBean to Map of topThreads MBeans
                topThreadsMap.put(rank, topThreadsBean);
                // register topThreadsBean with MBean server
                topThreadsMBeanHelper.registerMBean(topThreadsBean);

            } catch (Exception e) {
                LOGGER.severe("Failed to initialise top threads MBean with rank " + ranking +  " due to: " + e.getMessage());
            }
        }

        // instantiate Map of BlockedThread MBeans
        Map<Integer, BlockedThread> blockedThreadsMap = new HashMap<>();

		if (threadContentionMonitoringEnabled) {

			for (int rank = 1; rank <= blockedThreadCount; rank++) {

				// convert rank to String
				String ranking = String.valueOf(rank);

				// instantiate new MBeanHelper based on this type, statsType and rank
                MBeanHelper blockedThreadsMBeanHelper = null;
                try {
                    blockedThreadsMBeanHelper = new MBeanHelper(
                            Constants.JVM_STATS_TYPE, Constants.BLOCKED_THREAD_STATS_TYPE, ranking);
                    // instantiate new BlockedThread MBean
                    BlockedThread blockedThreadBean = new BlockedThread( config.mBeanServerConnection );
                    // add blockedThreadBean to Map of blockedThreads MBeans
                    blockedThreadsMap.put(rank, blockedThreadBean);
                    // register blockedThreadBean with MBean server
                    blockedThreadsMBeanHelper.registerMBean(blockedThreadBean);

                } catch (Exception e) {
                    LOGGER.severe("Failed to initialise blocked threads MBean with rank " + ranking +  " due to: " + e.getMessage());
                }
            }
		}

        // instantiate Map of HotMethod MBeans
        Map<Integer, HotMethod> hotMethodsMap = new HashMap<>();
        // instantiate new HotMethods object
        HotMethods hotMethods = null;
        try {
            hotMethods = new HotMethods( config.mBeanServerConnection, hotMethodsMap );
        } catch (IOException e) {
            LOGGER.severe("Failed to initialise hot methods MBean due to: " + e.getMessage());
        }

        if (hotMethodProfilingEnabled) {

            for (int rank = 1; rank <= hotMethodCount; rank++) {

                // convert rank to String
                String ranking = String.valueOf(rank);

                // instantiate new MBeanHelper based on this type, statsType and rank
                MBeanHelper hotMethodsMBeanHelper = null;
                try {
                    hotMethodsMBeanHelper = new MBeanHelper(
                            Constants.JVM_STATS_TYPE, Constants.HOT_METHOD_STATS_TYPE, ranking);
                    // instantiate new HotMethod MBean
                    HotMethod hotMethodMBean = new HotMethod( config.mBeanServerConnection );
                    // add hotMethod MBean to Map of hotMethods MBeans
                    hotMethodsMap.put(rank, hotMethodMBean);
                    // register hotMethod MBean with MBean server
                    hotMethodsMBeanHelper.registerMBean(hotMethodMBean);

                } catch (Exception e) {
                    LOGGER.severe("Failed to initialise hot method MBean with rank " + ranking +  " due to: " + e.getMessage());
                }
            }

            // instantiate new HotMethods object
            try {
                hotMethods = new HotMethods( config.mBeanServerConnection, hotMethodsMap );
            } catch (IOException e) {
                LOGGER.severe("Failed to initialise hot methods MBean due to: " + e.getMessage());
            }

        }

        try {
            // instantiate new MBeanHelper used to access ThreadStats MBean attributes and operations
            MBeanHelper threadStatsMBeanHelper = new MBeanHelper( Constants.JVM_STATS_TYPE, Constants.THREADS_STATS_TYPE );

            // instantiate new thread stats MBean
            ThreadStats threadStatsMBean;
            if (threadContentionMonitoringEnabled && ! hotMethodProfilingEnabled) {
                // instantiate new ThreadStats MBean with thread contention monitoring enabled
                threadStatsMBean = new ThreadStats(config.mBeanServerConnection, topThreadsMap, blockedThreadsMap);
            }
            else if (! threadContentionMonitoringEnabled && hotMethodProfilingEnabled) {
                // instantiate new ThreadStats MBean with hot method profiling enabled
                threadStatsMBean = new ThreadStats(config.mBeanServerConnection, topThreadsMap, hotMethods, hotMethodPollInterval);
            }
            else if (threadContentionMonitoringEnabled && hotMethodProfilingEnabled) {
                // instantiate new ThreadStats MBean with thread contention monitoring hot method profiling enabled
                threadStatsMBean = new ThreadStats(config.mBeanServerConnection, topThreadsMap, blockedThreadsMap, hotMethods, hotMethodPollInterval);
            }
            else {
                // instantiate new ThreadStats MBean
                threadStatsMBean = new ThreadStats(config.mBeanServerConnection, topThreadsMap);
            }

            // register threadStatsMBean with MBean server
            threadStatsMBeanHelper.registerMBean(threadStatsMBean);

            // instantiate and store new ThreadStatsMXBean proxy
            this.jvmStatsMBeans.add(JMX.newMBeanProxy(mbs, threadStatsMBeanHelper.getObjectName(), ThreadStatsMXBean.class));

        } catch (Exception e) {
            LOGGER.severe("Failed to initialise thread stats MBean due to: " + e.getMessage());
        }

    }

    /**
     * Init Memory Stats MBean
     */
    private void initMemoryStats( ) {

        // init heap stats MBean
        try {
            // instantiate new MBeanHelper used to access MemoryStats MBean attributes and operations
            MBeanHelper memoryStatsMBeanHelper = new MBeanHelper( Constants.JVM_STATS_TYPE, Constants.MEMORY_STATS_TYPE );
            // instantiate new MemoryStats MBean
            MemoryStats memoryStatsMBean = new MemoryStats( config.mBeanServerConnection );
            // register memoryStatsMBean with MBean server
            memoryStatsMBeanHelper.registerMBean(memoryStatsMBean);
            // instantiate and store new MemoryStatsMXBean proxy
            this.jvmStatsMBeans.add(JMX.newMBeanProxy(mbs, memoryStatsMBeanHelper.getObjectName(), MemoryStatsMXBean.class));

        } catch (Exception e) {
            LOGGER.severe("Failed to initialise memory stats MBean due to: " + e.getMessage());
        }

    }

    /**
     * Init Heap Stats MBean
     */
	private void initHeapStats( ) {

        // init heap stats MBean
        try {
            // instantiate new MBeanHelper used to access HeapStats MBean attributes and operations
            MBeanHelper heapStatsMBeanHelper = new MBeanHelper( Constants.JVM_STATS_TYPE, Constants.HEAP_STATS_TYPE );
            // instantiate new HeapStats MBean
            HeapStats heapStatsMBean = new HeapStats( config.mBeanServerConnection );
            // register heapStatsMBean with MBean server
            heapStatsMBeanHelper.registerMBean(heapStatsMBean);
            // instantiate and store new HeapStatsMXBean proxy
            this.jvmStatsMBeans.add(JMX.newMBeanProxy(mbs, heapStatsMBeanHelper.getObjectName(), HeapStatsMXBean.class));

        } catch (Exception e) {
            LOGGER.severe("Failed to initialise heap stats MBean due to: " + e.getMessage());
        }

	}

    /**
     * Init GC Stats MBean
     */
	private void initGCStats( ) {

        // init GC stats
        try {
            // instantiate new MBeanHelper used to access GCStats MBean attributes and operations
            MBeanHelper gcStatsMBeanHelper = new MBeanHelper( Constants.JVM_STATS_TYPE, Constants.GC_STATS_TYPE );
            // instantiate new GCStats MBean
            GCStats gcStatsMBean = new GCStats( config.mBeanServerConnection );
            // register gcStatsMBean with MBean server
            gcStatsMBeanHelper.registerMBean(gcStatsMBean);
            // instantiate and store new GCStatsMXBean proxy
            this.jvmStatsMBeans.add(JMX.newMBeanProxy(mbs, gcStatsMBeanHelper.getObjectName(), GCStatsMXBean.class));

        } catch (Exception e) {
            LOGGER.severe("Failed to initialise GC stats MBean due to: " + e.getMessage());
        }
    }

    /**
     * Init Agent Stats MBean
     */
    private void initAgentStats( ) {

        // init agent stats
        try {
            // instantiate new MBeanHelper used to access AgentStats MBean attributes and operations
            MBeanHelper agentStatsMBeanHelper = new MBeanHelper( Constants.JVM_STATS_TYPE, Constants.AGENT_STATS_TYPE );
            // instantiate new AgentStats MBean
            AgentStats agentStatsMBean = new AgentStats( jvmStatsMBeans );
            // register agentStatsMBean with MBean server
            agentStatsMBeanHelper.registerMBean(agentStatsMBean);
            // instantiate and store new AgentStatsMXBean proxy
            this.jvmStatsMBeans.add( JMX.newMBeanProxy(mbs, agentStatsMBeanHelper.getObjectName(), AgentStatsMXBean.class) );

        } catch (Exception e) {
            LOGGER.severe("Failed to initialise GC stats MBean due to: " + e.getMessage());
        }
    }

    /**
     * Init Stats Logger MBean
     */
    private void initStatsLogger( ) {

        // init stats logger
        try {
            // instantiate new MBeanHelper used to access StatsLogger MBean attributes and operations
            MBeanHelper statsLoggerMBeanHelper = new MBeanHelper( Constants.AGENT_TYPE, Constants.STATS_LOGGER_TYPE );
            // instantiate new StatsLogger MBean
            StatsLogger statsLoggerMBean = new StatsLogger( config );
            // register statsLoggerMBean with MBean server
            statsLoggerMBeanHelper.registerMBean(statsLoggerMBean);

        } catch (Exception e) {
            LOGGER.severe("Failed to initialise stats logger MBean due to: " + e.getMessage());
        }
    }

}
