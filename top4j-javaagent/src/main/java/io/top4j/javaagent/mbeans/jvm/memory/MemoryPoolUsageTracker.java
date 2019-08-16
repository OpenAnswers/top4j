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

package io.top4j.javaagent.mbeans.jvm.memory;

import java.util.logging.Logger;

import io.top4j.javaagent.mbeans.jvm.gc.GarbageCollectorMXBeanHelper;

import javax.management.MBeanServerConnection;

public class MemoryPoolUsageTracker {
	
	private String poolName;
	private String memoryPoolName;
	private MemoryPoolMXBeanHelper memoryPoolMxBeanHelper;
	private GarbageCollectorMXBeanHelper gcMXBeanHelper;
	private long memoryPoolIntervalUsage;
	private long memoryPoolPeakUsageUsed;
	private long lastMemoryPoolCollectionUsageUsed;
	private long lastGCCount;
	private long lastSystemTime;
	
	private static final Logger LOGGER = Logger.getLogger(MemoryPoolUsageTracker.class.getName());
	
	public MemoryPoolUsageTracker (MBeanServerConnection mbsc, String poolName) throws Exception {
		
		LOGGER.fine("Initialising " + poolName + " memory usage tracker....");
		
		// store pool name
		this.setPoolName(poolName);
		// initialise memory pool interval usage
		this.setMemoryPoolIntervalUsage(0);
		
		// instantiate new MemoryPoolMXBeanHelper
		MemoryPoolMXBeanHelper memoryPoolMxBeanHelper = new MemoryPoolMXBeanHelper( mbsc );
		this.memoryPoolMxBeanHelper = memoryPoolMxBeanHelper;
		
		// instantiate new GarbageCollectorMXBeanHelper
		try {
			this.gcMXBeanHelper = new GarbageCollectorMXBeanHelper( mbsc );
		} catch (Exception e) {
			throw new Exception( "Failed to initialise Memory Pool Usage Tracker due to: " + e.getMessage() );
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
			throw new IllegalArgumentException("Unsupported pool name \"" + poolName + "\" provided to MemoryPoolUsageTracker constructor");
			
		}
		
		// store memory pool name
		LOGGER.fine("Memory Pool Name = " + memoryPoolName);
		this.setMemoryPoolName(memoryPoolName);
		
		// get and store current GC count
		long gcCount = this.getGCCount();
		this.setLastGCCount(gcCount);
		
	}

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String memoryPoolName) {
		this.poolName = memoryPoolName;
	}

	public String getMemoryPoolName() {
		return memoryPoolName;
	}

	public void setMemoryPoolName(String memoryPoolName) {
		this.memoryPoolName = memoryPoolName;
	}

	public synchronized long getMemoryPoolIntervalUsage() {
		return memoryPoolIntervalUsage;
	}

	public synchronized void setMemoryPoolIntervalUsage(long memoryPoolIntervalUsage) {
		this.memoryPoolIntervalUsage = memoryPoolIntervalUsage;
	}
	
	public synchronized void resetMemoryPoolIntervalUsage() {
		this.memoryPoolIntervalUsage = 0;
	}
	
	public synchronized long getAndResetMemoryPoolIntervalUsage() {
		
		long memoryPoolIntervalUsage = this.memoryPoolIntervalUsage;
		this.memoryPoolIntervalUsage = 0;
		return memoryPoolIntervalUsage;
		
	}

	public synchronized long getMemoryPoolPeakUsageUsed() {
		return memoryPoolPeakUsageUsed;
	}

	public synchronized void setMemoryPoolPeakUsageUsed(long memoryPoolPeakUsageUsed) {
		this.memoryPoolPeakUsageUsed = memoryPoolPeakUsageUsed;
	}

	public synchronized long getLastMemoryPoolCollectionUsageUsed() {
		return lastMemoryPoolCollectionUsageUsed;
	}

	public synchronized void setLastMemoryPoolCollectionUsageUsed(long lastMemoryPoolCollectionUsageUsed) {
		this.lastMemoryPoolCollectionUsageUsed = lastMemoryPoolCollectionUsageUsed;
	}

	public long getLastGCCount() {
		return lastGCCount;
	}

	public void setLastGCCount(long lastGCCount) {
		this.lastGCCount = lastGCCount;
	}

	public long getLastSystemTime() {
		return lastSystemTime;
	}

	public void setLastSystemTime(long lastSystemTime) {
		this.lastSystemTime = lastSystemTime;
	}
	
	public synchronized void update() {
		
		long memoryPoolUsage;
		
		long memoryPoolCollectionUsed = getMemoryPoolCollectionUsed( );
		long memoryPoolPeakUsed = getMemoryPoolPeakUsed();
		long gcCount = getGCCount();
		long intervalGCCount = gcCount - lastGCCount;
		
		LOGGER.finer(poolName + " lastMemoryPoolCollectionUsed = " + lastMemoryPoolCollectionUsageUsed);
		LOGGER.finer(poolName + " memoryPoolCollectionUsed = " + memoryPoolCollectionUsed);
		LOGGER.finer(poolName + " memoryPoolPeakUsed = " + memoryPoolPeakUsed);
		LOGGER.finer(poolName + " intervalGCCount = " + intervalGCCount);
		
		// calculate memory pool usage since last update
		if (poolName.equals("Survivor")) {
			memoryPoolUsage = memoryPoolCollectionUsed * intervalGCCount;
		}
		else {
			memoryPoolUsage = ( memoryPoolPeakUsed - lastMemoryPoolCollectionUsageUsed ) * intervalGCCount;
		}
		LOGGER.finer(poolName + " memoryPoolUsage = " + memoryPoolUsage);
		
		// add memory pool usage to running total
		this.memoryPoolIntervalUsage += memoryPoolUsage;
		
		// store this memory pool collection used - the amount of memory in use following the last GC 
		this.lastMemoryPoolCollectionUsageUsed = memoryPoolCollectionUsed;
		
		// store this GC count
		this.lastGCCount = gcCount;
		
		// reset memory pool peak usage
		resetMemoryPoolPeakUsage();
		
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
            LOGGER.fine("Unable to retrieve " + poolName + " memory pool collection used due to: " + e.getMessage());
        }

		return memoryPoolCollectionUsed;
	}
	
	private long getMemoryPoolPeakUsed() {
		
		long memoryPoolPeakUsed = 0;

        try {
            switch (poolName) {

                case "Nursery":
                    memoryPoolPeakUsed = memoryPoolMxBeanHelper.getNurseryPeakUsed();
                    break;
                case "Survivor":
                    memoryPoolPeakUsed = memoryPoolMxBeanHelper.getSurvivorPeakUsed();
                    break;
                case "Tenured":
                    memoryPoolPeakUsed = memoryPoolMxBeanHelper.getTenuredPeakUsed();
                    break;

            }
        } catch (Exception e) {
            LOGGER.fine("Unable to retrieve " + poolName + " memory pool peak used due to: " + e.getMessage());
        }

        return memoryPoolPeakUsed;
	}
	
	private void resetMemoryPoolPeakUsage() {

        try {
            switch (poolName) {

                case "Nursery":
                    memoryPoolMxBeanHelper.resetNurseryPeakUsage();
                    break;
                case "Survivor":
                    memoryPoolMxBeanHelper.resetSurvivorPeakUsage();
                    break;
                case "Tenured":
                    memoryPoolMxBeanHelper.resetTenuredPeakUsage();
                    break;

            }
        } catch (Exception e) {
            LOGGER.fine("Unable to reset " + poolName + " memory pool peak usage due to: " + e.getMessage());
        }

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
            LOGGER.fine("Unable to retrieve GC count due to: " + e.getMessage());
		}

		return gcCount;
	}

}
