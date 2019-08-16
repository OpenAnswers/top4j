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

package io.top4j.javaagent.mbeans.jvm.heap;

import io.top4j.javaagent.profiler.CpuTime;

import javax.management.MBeanServerConnection;
import java.util.logging.Logger;

public class HeapStats implements HeapStatsMXBean {

	private HeapUtilisation heapUtilisation;
    private CpuTime cpuTime = new CpuTime();
	private double mBeanCpuTime;

	private static final Logger LOGGER = Logger.getLogger(HeapStats.class.getName());

	public HeapStats( MBeanServerConnection mbsc ) throws Exception {
		
		LOGGER.fine("Initialising Heap Stats....");
		
		// instantiate new HeapUtilisation to store heap utilisation
		this.heapUtilisation = new HeapUtilisation( mbsc );

	}
	
	/** Update Heap stats. */
    public synchronized void update( ) {

        // initialise thread CPU timer
    	cpuTime.init();

    	LOGGER.fine("Updating Heap stats....");
    	
    	// update heap utilisation
    	this.heapUtilisation.update();

        // update heap stats CPU time
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
	public void setEdenSpaceUtil(double edenSpaceUtil) {
		this.heapUtilisation.setEdenSpaceUtil( edenSpaceUtil );
	}

	@Override
	public double getEdenSpaceUtil() {
		return this.heapUtilisation.getEdenSpaceUtil();
	}

	@Override
	public void setSurvivorSpaceUtil(double survivorSpaceUtil) {
		this.heapUtilisation.setSurvivorSpaceUtil( survivorSpaceUtil );
	}

	@Override
	public double getSurvivorSpaceUtil() {
		return this.heapUtilisation.getSurvivorSpaceUtil();
	}

	@Override
	public void setTenuredHeapUtil(double tenuredHeapUtil) {
		this.heapUtilisation.setTenuredHeapUtil( tenuredHeapUtil );
	}

	@Override
	public double getTenuredHeapUtil() {
		return this.heapUtilisation.getTenuredHeapUtil();
	}

}
