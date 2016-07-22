package io.top4j.javaagent.mbeans.jvm.memory;

import java.util.logging.Logger;

import io.top4j.javaagent.config.Constants;
import io.top4j.javaagent.mbeans.jvm.gc.GCTimeBean;
import io.top4j.javaagent.mbeans.jvm.gc.GarbageCollectorMXBeanHelper;

import javax.management.MBeanServerConnection;

public class MemoryPoolAllocationRate {
	
	volatile private double memoryPoolAllocationRate;
	private String poolName;
	private String memoryPoolName;
	private MemoryPoolUsageTracker memoryPoolUsageTracker;
	private MemoryPoolMXBeanHelper memoryPoolMxBeanHelper;
	private GarbageCollectorMXBeanHelper gcMXBeanHelper;
	private GCTimeBean gcTimeBean;
	private MemoryPoolUsageBean memoryPoolUsageBean;
	
	private static final Logger LOGGER = Logger.getLogger(MemoryPoolAllocationRate.class.getName());
	
	public MemoryPoolAllocationRate ( MBeanServerConnection mbsc, String poolName, MemoryPoolUsageTracker memoryPoolUsageTracker ) throws Exception {
		
		LOGGER.fine("Initialising " + poolName + " allocation rate....");
		
		// store poolName
		this.poolName = poolName;
		
		// store memoryPoolUsageTracker
		this.memoryPoolUsageTracker = memoryPoolUsageTracker;
		
		// instantiate new MemoryPoolMXBeanHelper
        try {
		    this.memoryPoolMxBeanHelper = new MemoryPoolMXBeanHelper( mbsc );
        } catch (Exception e) {
            throw new Exception( "Failed to initialise " + poolName + " Allocation Rate stats collector due to: " + e.getMessage() );
        }

		// instantiate new GarbageCollectorMXBeanHelper and GCTimeBean
		try {
			this.gcMXBeanHelper = new GarbageCollectorMXBeanHelper( mbsc );
			this.gcTimeBean = new GCTimeBean( mbsc );
		} catch (Exception e) {
			throw new Exception( "Failed to initialise " + poolName + " Allocation Rate stats collector due to: " + e.getMessage() );
		}

		String memoryPoolName;
		
		switch (poolName) {
		
		case "Nursery":
			memoryPoolName = memoryPoolMxBeanHelper.getNurseryPoolName();
			break;
		case "Survivor":
			memoryPoolName = memoryPoolMxBeanHelper.getSurvivorSpacePoolName();
			break;
		case "Tenured":
			memoryPoolName = memoryPoolMxBeanHelper.getTenuredPoolName();
			break;
		default:
			throw new IllegalArgumentException("Unsupported pool name \"" + poolName + "\" provided to MemoryPoolAllocationRate constructor");
			
		}
		
		LOGGER.fine("Memory Pool Name = " + memoryPoolName);
		this.setMemoryPoolName(memoryPoolName);
		
		this.memoryPoolUsageBean = new MemoryPoolUsageBean( mbsc, memoryPoolName );
		
	}
	
	/** Update Memory Pool Allocation Rate. */
    public void update( ) {
    	
    	long memoryPoolUsageUsed;
    	long memoryPoolUsageCommitted;
    	long memoryPoolCollectionUsed;
    	long systemTime = System.currentTimeMillis();
    	double intervalSystemTimeSecs;
    	long meanMemoryPoolUsageCommitted;
    	long meanMemoryPoolCollectionUsed;
    	long gcCount;
    	long intervalGCCount;
    	long intervalMemoryUsageUsed;

    	memoryPoolUsageUsed = getMemoryPoolUsageUsed();
		memoryPoolUsageCommitted = getMemoryPoolUsageCommitted();
		memoryPoolCollectionUsed = getMemoryPoolCollectionUsed();
		LOGGER.finer(poolName + " Used = " + memoryPoolUsageUsed);
		LOGGER.finer(poolName + " Committed = " + memoryPoolUsageCommitted);
		LOGGER.finer(poolName + " CollectionUsed = " + memoryPoolCollectionUsed);
		
		// calculate intervalSystemTimeSecs in seconds
		intervalSystemTimeSecs = ( (double) systemTime - (double) memoryPoolUsageBean.getLastSystemTime() ) / 1000;
		LOGGER.finest("Nursery System Time = " + systemTime);
		LOGGER.finest("Nursery Last System Time = " + memoryPoolUsageBean.getLastSystemTime());
		
		// calculate meanMemoryPoolUsageCommitted - the mean memory pool usage committed value during this interval
		// i.e. the mean memory pool usage high water mark during this interval
		meanMemoryPoolUsageCommitted = ( memoryPoolUsageCommitted + memoryPoolUsageBean.getLastMemoryPoolUsageCommitted() ) / 2;
		LOGGER.finer("Mean Nursery Committed = " + meanMemoryPoolUsageCommitted);
		
		// calculate meanMemoryPoolCollectionUsed - the mean memory pool collection usage used value during this interval
		// i.e. the mean memory pool usage low water mark during this interval
		meanMemoryPoolCollectionUsed = ( memoryPoolCollectionUsed + memoryPoolUsageBean.getLastCollectionUsageUsed() ) / 2;
		LOGGER.finer("Mean Nursery Collection Used = " + meanMemoryPoolCollectionUsed);
		
		// calculate intervalGCCount - the number of GC cycles since the last update
		gcCount = getGCCount();
		intervalGCCount = gcCount - gcTimeBean.getLastGCCount();
		gcTimeBean.setLastGCCount(gcCount);
    	
		if (intervalGCCount == 0) {
			intervalMemoryUsageUsed = memoryPoolUsageUsed - memoryPoolUsageBean.getLastMemoryPoolUsageUsed();
		}
		else {
			// update memoryPoolUsageTracker - just in case the CollectionListener hasn't been able to keep up with demand
			memoryPoolUsageTracker.update();
			intervalMemoryUsageUsed = memoryPoolUsageTracker.getAndResetMemoryPoolIntervalUsage();
		}
		LOGGER.finer("Interval " + poolName + " GC Count = " + intervalGCCount);
		LOGGER.finer("Interval " + poolName + " Usage = " + intervalMemoryUsageUsed);
		LOGGER.finer("Interval " + poolName + " System Time Secs = " + intervalSystemTimeSecs);
		
		// calculate memoryAllocationRate in MB/s
		this.memoryPoolAllocationRate = calculateMemoryPoolAllocationRate( intervalMemoryUsageUsed, intervalSystemTimeSecs );
		LOGGER.fine(poolName + " Memory Pool Allocation Rate = " + this.memoryPoolAllocationRate + " MB/s");
		// persist this memory pool usage
		memoryPoolUsageBean.setLastMemoryPoolUsageUsed(memoryPoolUsageUsed);
		memoryPoolUsageBean.setLastMemoryPoolUsageCommitted(memoryPoolUsageCommitted);
		memoryPoolUsageBean.setLastSystemTime(systemTime);
    }

	public double getMemoryPoolAllocationRate() {
		return memoryPoolAllocationRate;
	}

	public void setMemoryPoolAllocationRate(double memoryPoolAllocationRate) {
		this.memoryPoolAllocationRate = memoryPoolAllocationRate;
	}
	
	private long getMemoryPoolUsageUsed() {
		
		long memoryPoolUsageUsed = 0;

        try {
            switch (poolName) {

                case "Nursery":
                    memoryPoolUsageUsed = memoryPoolMxBeanHelper.getNurseryHeapUsed();
                    break;
                case "Survivor":
                    memoryPoolUsageUsed = memoryPoolMxBeanHelper.getSurvivorSpaceUsed();
                    break;
                case "Tenured":
                    memoryPoolUsageUsed = memoryPoolMxBeanHelper.getTenuredHeapUsed();
                    break;

            }
        } catch (Exception e) {
            LOGGER.fine("Unable to retrieve " + poolName + " memory pool usage used due to: " + e.getMessage() );
        }

        return memoryPoolUsageUsed;
		
	}
	
	private long getMemoryPoolUsageCommitted() {
		
		long memoryPoolUsageCommitted = 0;

        try {
            switch (poolName) {

                case "Nursery":
                    memoryPoolUsageCommitted = memoryPoolMxBeanHelper.getNurseryHeapCommitted();
                    break;
                case "Survivor":
                    memoryPoolUsageCommitted = memoryPoolMxBeanHelper.getSurvivorSpaceCommitted();
                    break;
                case "Tenured":
                    memoryPoolUsageCommitted = memoryPoolMxBeanHelper.getTenuredHeapCommitted();
                    break;

            }
        } catch (Exception e) {
            LOGGER.fine("Unable to retrieve " + poolName + " memory pool usage committed due to: " + e.getMessage() );
        }

        return memoryPoolUsageCommitted;
	}
	
	private long getMemoryPoolCollectionUsed() {
		
		long memoryPoolCollectionUsed = 0;

        try {
            switch (poolName) {

                case "Nursery":
                    memoryPoolCollectionUsed = memoryPoolMxBeanHelper.getNurseryCollectionUsed();
                    break;
                case "Survivor":
                    memoryPoolCollectionUsed = memoryPoolMxBeanHelper.getSurvivorCollectionUsed();
                    break;
                case "Tenured":
                    memoryPoolCollectionUsed = memoryPoolMxBeanHelper.getTenuredCollectionUsed();
                    break;

            }
        } catch (Exception e) {
            LOGGER.fine("Unable to retrieve " + poolName + " memory pool collection used due to: " + e.getMessage() );
        }

        return memoryPoolCollectionUsed;
	}
	
	private long getGCCount() {
		
		long gcCount = 0;

		try {
				switch (poolName) {

					case "Nursery":
						gcCount = gcMXBeanHelper.getNurseryGCCount();
						break;
					case "Survivor":
						gcCount = gcMXBeanHelper.getNurseryGCCount();
						break;
					case "Tenured":
						gcCount = gcMXBeanHelper.getTenuredGCCount();
						break;

				}

		} catch (Exception e) {
			LOGGER.fine("Unable to retrieve " + poolName + " GC count due to: " + e.getMessage() );
		}
		return gcCount;
	}

	public String getMemoryPoolName() {
		return memoryPoolName;
	}

	public void setMemoryPoolName(String memoryPoolName) {
		this.memoryPoolName = memoryPoolName;
	}
	
	private double calculateMemoryPoolAllocationRate( long intervalMemoryUsageUsed, double intervalSystemTimeSecs ) {

		// calculate memory pool allocation rate in MB/s
		return ( (double) intervalMemoryUsageUsed / intervalSystemTimeSecs ) / Constants.ONE_MEGA_BYTE;
		
	}

}
