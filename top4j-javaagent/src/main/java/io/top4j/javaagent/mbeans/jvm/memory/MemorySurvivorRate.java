package io.top4j.javaagent.mbeans.jvm.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.NotificationEmitter;

import io.top4j.javaagent.mbeans.jvm.gc.CollectionUsageListener;

public class MemorySurvivorRate {

	volatile private double memorySurvivorRate;
	private MemorySurvivorBean memorySurvivor;
	
	private static final Logger LOGGER = Logger.getLogger(MemorySurvivorRate.class.getName());
	
	public MemorySurvivorRate ( MBeanServerConnection mbsc ) throws Exception {
		
		// instantiate new MemorySurvivorBean
		MemorySurvivorBean memorySurvivor = new MemorySurvivorBean();
		
		// instantiate new MemoryPoolMXBeanHelper
		MemoryPoolMXBeanHelper memoryPoolMxBeanHelper = new MemoryPoolMXBeanHelper( mbsc );
				
		// Register CollectionUsageListener with MemoryMXBean
	    MemoryMXBean memoryMXBean = ManagementFactory.getPlatformMXBean( mbsc, MemoryMXBean.class );
	    NotificationEmitter emitter = (NotificationEmitter) memoryMXBean;
	    CollectionUsageListener listener = new CollectionUsageListener( mbsc, memorySurvivor );
	    emitter.addNotificationListener(listener, null, null);

		long threshold = 1;
		memoryPoolMxBeanHelper.setSurvivorCollectionUsageThreshold(threshold);
	    
	    this.memorySurvivor = memorySurvivor;
		
	}

	public double getMemorySurvivorRate() {
		return memorySurvivorRate;
	}

	public void setMemorySurvivorRate(double memorySurvivorRate) {
		this.memorySurvivorRate = memorySurvivorRate;
	}
	
	/** Update Memory Survivor Rate. */
    public void update( ) {
    	
    	long systemTime = System.currentTimeMillis();
    	double intervalSystemTimeSecs;
    	long intervalSurvivors;
    	
    	// calculate intervalSystemTimeSecs in seconds
    	intervalSystemTimeSecs = ( (double) systemTime - (double) memorySurvivor.getLastSystemTime() ) / 1000;
    	LOGGER.finest("Survivor Rate System Time = " + systemTime);
    	LOGGER.finest("Survivor Rate Last System Time = " + memorySurvivor.getLastSystemTime());
    	
    	// get and reset intervalSurvivors
    	intervalSurvivors = memorySurvivor.getAndResetSurvivors();
    	
    	// calculate memory survivor rate
    	this.memorySurvivorRate = calculateMemorySurvivorRate( intervalSurvivors, intervalSystemTimeSecs );
    	
    	this.memorySurvivor.setLastSystemTime(systemTime);
    	
    }
    
    private double calculateMemorySurvivorRate( long intervalSurvivors, double intervalSystemTimeSecs ) {
		
		return ( (double) intervalSurvivors / intervalSystemTimeSecs ) / 1048576;
		
	}

}
