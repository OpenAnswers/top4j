/*
 * Copyright (c) 2019 Open Answers Ltd. https://www.openanswers.co.uk
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
