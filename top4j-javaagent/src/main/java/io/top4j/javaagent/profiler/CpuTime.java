/*
 * Copyright (c) 2019 Open Answers Ltd.
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
