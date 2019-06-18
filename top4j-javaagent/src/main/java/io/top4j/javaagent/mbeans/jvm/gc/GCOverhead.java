package io.top4j.javaagent.mbeans.jvm.gc;

import javax.management.MBeanServerConnection;
import java.util.logging.Logger;

public class GCOverhead {
	
	volatile private double gcOverhead;
	private GCTimeBean gcTimeBean;
	private GarbageCollectorMXBeanHelper gcMXBeanHelper;
	
	private static final Logger LOGGER = Logger.getLogger(GCOverhead.class.getName());
	
	public GCOverhead ( MBeanServerConnection mbsc ) throws Exception {
		
		LOGGER.fine("Initialising GC Overhead....");
		
		// instantiate new GarbageCollectorMXBeanHelper and GCTimeBean
		try {
			this.gcMXBeanHelper = new GarbageCollectorMXBeanHelper( mbsc );
			this.gcTimeBean = new GCTimeBean( mbsc );
		} catch (Exception e) {
			throw new Exception( "Failed to initialise GC Overhead stats collector due to: " + e.getMessage() );
		}

	}
	
	/** Update GC Overhead. */
    public void update( ) {
    	
    	long systemTime = System.currentTimeMillis();
    	long gcTime;
    	long intervalGCTime;
    	long intervalSystemTime;
    	
    	gcTime = gcMXBeanHelper.getGCTime();
    	intervalGCTime = gcTime - gcTimeBean.getLastGCTime();
    	intervalSystemTime = systemTime - gcTimeBean.getLastSystemTime();
    	LOGGER.finer("GC Overhead Interval GC Time = " + intervalGCTime);
    	LOGGER.finer("GC Overhead Interval System Time = " + intervalSystemTime);
    	gcOverhead = calculateGCOverhead( intervalGCTime, intervalSystemTime );
    	LOGGER.fine("GC Overhead = " + gcOverhead + "%");
    	gcTimeBean.setLastGCTime(gcTime);
    	gcTimeBean.setLastSystemTime(systemTime);
    	
    }

	public double getGcOverhead( ) {
		return gcOverhead;
	}

	public void setGcOverhead(double gcOverhead) {
		this.gcOverhead = gcOverhead;
	}
	
	private double calculateGCOverhead( long intervalGCTime, long intervalSystemTime ) {
		
		return ( (double) intervalGCTime / intervalSystemTime ) * 100;
		
	}

}
