package io.top4j.javaagent.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class CpuTime {

	private long lastCPUTime;
	private ThreadMXBean threadMXBean;

	public CpuTime() {
		
		ThreadMXBean threadMXBean =
	            ManagementFactory.getThreadMXBean( );
		this.threadMXBean = threadMXBean;
        // initialise CPU time
        init();

	}
	
	/*
	 * reset CPU time
	 * 
	 */
	public void init() {
		
		// get and store current thread CPU time
		this.lastCPUTime = threadMXBean.getCurrentThreadCpuTime();
				
	}
	
	/*
	 * get CPU time
	 * 
	 */
	public long get( ) {
		
		// get current thread CPU time
		long cpuTime = threadMXBean.getCurrentThreadCpuTime();
		
		// calculate CPU burn in nanoseconds
		long cpuBurn = cpuTime - lastCPUTime;

		return cpuBurn;

	}

    /*
     * get CPU time in milliseconds
     *
     */
    public double getMillis( ) {

        // get current thread CPU time
        long cpuTime = threadMXBean.getCurrentThreadCpuTime();

        // calculate CPU burn in milliseconds
        double cpuBurn = ( (double) cpuTime - lastCPUTime ) / 1000000;

        return cpuBurn;

    }

}
