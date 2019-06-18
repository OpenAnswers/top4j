package io.top4j.javaagent.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.logging.Logger;

import io.top4j.javaagent.mbeans.logger.StatsLogger;

public class CPUTimer {
	
	private long lastCurrentTime;
	private long lastCPUTime;
	private ThreadMXBean threadMXBean;
	
	private static final Logger LOGGER = Logger.getLogger(StatsLogger.class.getName());
	
	public CPUTimer( ) {
		
		ThreadMXBean threadMXBean =
	            ManagementFactory.getThreadMXBean( );
		this.threadMXBean = threadMXBean;
		
	}
	
	/*
	 * start CPU timer
	 * 
	 */
	public void start( ) {
		
		// get current thread CPU time
		long cpuTime = threadMXBean.getCurrentThreadCpuTime();
		
		// store CPU time
		this.lastCPUTime = cpuTime;
				
		// get current system time
		long currentTime = System.nanoTime();
		
		// store system time
		this.lastCurrentTime = currentTime;
		
	}
	
	/*
	 * stop CPU timer
	 * 
	 */
	public void stop( ) {
		
		// get current thread CPU time
		long cpuTime = threadMXBean.getCurrentThreadCpuTime();
		
		// get current system time
		long currentTime = System.nanoTime();
		
		// calculate CPU burn in milliseconds
		long cpuBurn = cpuTime - lastCPUTime;
		
		// calculate elapsed time in milliseconds
		long elapsedTime = currentTime - lastCurrentTime;
		
		// log cpuBurn and elapsedTime
		LOGGER.info("CPU Burn = " + cpuBurn + " ns, Elapsed Time = " + elapsedTime + " ns");
		
	}

}
