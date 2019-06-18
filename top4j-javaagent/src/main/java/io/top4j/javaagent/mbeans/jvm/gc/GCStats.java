package io.top4j.javaagent.mbeans.jvm.gc;

import io.top4j.javaagent.profiler.CpuTime;

import javax.management.MBeanServerConnection;
import java.util.logging.*;


public class GCStats implements GCStatsMXBean {

	private GCOverhead gcOverhead;
	private GCPauseTime gcPauseTime;
	private CpuTime cpuTime = new CpuTime();
	private double mBeanCpuTime;
	
	private static final Logger LOGGER = Logger.getLogger(GCStats.class.getName());
	
	public GCStats ( MBeanServerConnection mbsc ) throws Exception {
		
		LOGGER.fine("Initialising GC stats....");
		
		// instantiate new GC Overhead
		GCOverhead gcOverhead = new GCOverhead( mbsc );
		
		// instantiate new GC Pause Time
		GCPauseTime gcPauseTime = new GCPauseTime( mbsc );
		
		this.gcOverhead = gcOverhead;
		this.gcPauseTime = gcPauseTime;
		
	}
	
	/** Update GC stats. */
    public synchronized void update( ) {

        // initialise thread CPU timer
    	cpuTime.init();

    	LOGGER.fine("Updating GC stats....");
    	
    	// update GC Overhead
    	gcOverhead.update();
    	
    	// update GC pause time stats
    	gcPauseTime.update();

        // update GC stats CPU time
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
	public void setGcOverhead(double gcOverhead) {
		this.gcOverhead.setGcOverhead( gcOverhead );
		
	}

	@Override
	public double getGcOverhead() {
		return this.gcOverhead.getGcOverhead();
	}

	@Override
	public void setMeanNurseryGCTime(double meanNurseryGCTime) {
		this.gcPauseTime.setMeanNurseryGCTime( meanNurseryGCTime );
	}

	@Override
	public double getMeanNurseryGCTime() {
		return this.gcPauseTime.getMeanNurseryGCTime();
	}

	@Override
	public void setMeanTenuredGCTime(double meanTenuredGCTime) {
		gcPauseTime.setMeanTenuredGCTime( meanTenuredGCTime );
	}

	@Override
	public double getMeanTenuredGCTime() {
		return this.gcPauseTime.getMeanTenuredGCTime();
	}
    
}
