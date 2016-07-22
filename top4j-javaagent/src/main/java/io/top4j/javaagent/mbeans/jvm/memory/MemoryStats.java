package io.top4j.javaagent.mbeans.jvm.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import java.util.logging.*;

import javax.management.MBeanServerConnection;
import javax.management.NotificationEmitter;

import io.top4j.javaagent.listener.CollectionListener;
import io.top4j.javaagent.profiler.CpuTime;

public class MemoryStats implements MemoryStatsMXBean {

	private MemoryPoolAllocationRate memoryAllocationRate;
	private MemoryPoolAllocationRate memorySurvivorRate;
	private MemoryPoolAllocationRate memoryPromotionRate;
	private CpuTime cpuTime = new CpuTime();
	private double mBeanCpuTime;
	
	private static final Logger LOGGER = Logger.getLogger(MemoryStats.class.getName());
	
	public MemoryStats ( MBeanServerConnection mbsc ) throws Exception {
		
		LOGGER.fine("Initialising Memory stats....");
		
		// instantiate new MemoryPoolUsageTracker to track nursery pool usage
		MemoryPoolUsageTracker nurseryPoolUsageTracker = new MemoryPoolUsageTracker( mbsc, "Nursery" );

		// instantiate new MemoryPoolUsageTracker to track nursery pool usage
		MemoryPoolUsageTracker survivorPoolUsageTracker = new MemoryPoolUsageTracker( mbsc, "Survivor" );

		// instantiate new MemoryPoolUsageTracker to track nursery pool usage
		MemoryPoolUsageTracker tenuredPoolUsageTracker = new MemoryPoolUsageTracker( mbsc, "Tenured" );

		// instantiate new MemoryPoolAllocationRate to store memory allocation rate
		MemoryPoolAllocationRate memoryAllocationRate = new MemoryPoolAllocationRate( mbsc, "Nursery", nurseryPoolUsageTracker );

		// instantiate new MemorySurvivorRate to store memory survivor rate
		MemoryPoolAllocationRate memorySurvivorRate = new MemoryPoolAllocationRate( mbsc, "Survivor", survivorPoolUsageTracker );

		// instantiate new MemoryPoolAllocationRate to store memory promotion rate
		MemoryPoolAllocationRate memoryPromotionRate = new MemoryPoolAllocationRate( mbsc, "Tenured", tenuredPoolUsageTracker );

		// instantiate new MemoryPoolMXBeanHelper
		MemoryPoolMXBeanHelper memoryPoolMxBeanHelper = new MemoryPoolMXBeanHelper( mbsc );

		// register CollectionListener with MemoryMXBean
	    MemoryMXBean memoryMXBean = ManagementFactory.getPlatformMXBean( mbsc, MemoryMXBean.class );
	    NotificationEmitter emitter = (NotificationEmitter) memoryMXBean;
	    CollectionListener listener = new CollectionListener( mbsc, nurseryPoolUsageTracker, survivorPoolUsageTracker, tenuredPoolUsageTracker );
	    emitter.addNotificationListener(listener, null, null);

	    // set memory pool usage thresholds
		int memoryUsageThreshold = 1;
		memoryPoolMxBeanHelper.setTenuredUsageThreshold(memoryUsageThreshold);

	    // set memory pool collection usage thresholds
		int collectionUsageThreshold = 1;
		memoryPoolMxBeanHelper.setNurseryCollectionUsageThreshold(collectionUsageThreshold);
	    memoryPoolMxBeanHelper.setSurvivorCollectionUsageThreshold(collectionUsageThreshold);
	    memoryPoolMxBeanHelper.setTenuredCollectionUsageThreshold(collectionUsageThreshold);

		this.memoryAllocationRate = memoryAllocationRate;
		this.memorySurvivorRate = memorySurvivorRate;
		this.memoryPromotionRate = memoryPromotionRate;

	}
	
	/** Update Memory stats. */
    public synchronized void update( ) {

		// initialise thread CPU timer
    	cpuTime.init();

    	LOGGER.fine("Updating Memory stats....");
    	
    	// update memory allocation rate
    	this.memoryAllocationRate.update();
    	
    	// update memory survivor rate
    	this.memorySurvivorRate.update();
    	
    	// update memory promotion rate
    	this.memoryPromotionRate.update();

		// update memory stats CPU time
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

	@Override
	public void setMemoryAllocationRate(double memoryAllocationRate) {
		this.memoryAllocationRate.setMemoryPoolAllocationRate( memoryAllocationRate );
	}

	@Override
	public double getMemoryAllocationRate() {
		return this.memoryAllocationRate.getMemoryPoolAllocationRate();
	}

	@Override
	public void setMemorySurvivorRate(double memorySurvivorRate) {
		this.memorySurvivorRate.setMemoryPoolAllocationRate( memorySurvivorRate );
	}

	@Override
	public double getMemorySurvivorRate() {
		return this.memorySurvivorRate.getMemoryPoolAllocationRate();
	}

	@Override
	public void setMemoryPromotionRate(double memoryPromotionRate) {
		this.memoryPromotionRate.setMemoryPoolAllocationRate( memoryPromotionRate );
	}

	@Override
	public double getMemoryPromotionRate() {
		return this.memoryPromotionRate.getMemoryPoolAllocationRate();
	}
    
}