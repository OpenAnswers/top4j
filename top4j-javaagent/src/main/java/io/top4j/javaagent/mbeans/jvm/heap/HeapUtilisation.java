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

package io.top4j.javaagent.mbeans.jvm.heap;

import io.top4j.javaagent.exception.MBeanInitException;
import io.top4j.javaagent.mbeans.jvm.memory.MemoryPoolMXBeanHelper;

import javax.management.MBeanServerConnection;
import java.util.logging.Logger;

public class HeapUtilisation {
	
	volatile private double edenSpaceUtil;
	volatile private double survivorSpaceUtil;
	volatile private double tenuredHeapUtil;
	private MemoryPoolMXBeanHelper memoryPoolMxBeanHelper;
	
	private static final Logger LOGGER = Logger.getLogger(HeapUtilisation.class.getName());
	
	public HeapUtilisation ( MBeanServerConnection mbsc ) throws Exception {
		
		LOGGER.fine("Initialising Heap Utilisation....");
		
		// instantiate new MemoryPoolMXBeanHelper
		try {
			this.memoryPoolMxBeanHelper = new MemoryPoolMXBeanHelper( mbsc );
		} catch (Exception e) {
			throw new MBeanInitException( e, "Failed to initialise heap utilisation stats collector due to: " + e.getMessage() );
		}

	}
	
	/** Update Heap Utilisation. */
    public void update( ) {

		// update eden space util
		long edenSpaceUsed = 0;
		try {
			edenSpaceUsed = memoryPoolMxBeanHelper.getNurseryCollectionUsed();
		} catch (Exception e) {
			LOGGER.fine("WARNING: Unable to retrieve nursery collection used from MemoryPool MBean due to: " + e.getMessage() );
		}
		long edenSpaceCommitted = 0;
		try {
			edenSpaceCommitted = memoryPoolMxBeanHelper.getNurseryCollectionCommitted();
		} catch (Exception e) {
			LOGGER.fine("WARNING: Unable to retrieve nursery collection committed from MemoryPool MBean due to: " + e.getMessage() );
		}
		LOGGER.finer("Eden Collection Used = " + edenSpaceUsed);
		LOGGER.finer("Eden Collection Committed = " + edenSpaceCommitted);
		if (edenSpaceCommitted > 0) {
			this.edenSpaceUtil = calculateHeapUtil( edenSpaceUsed, edenSpaceCommitted );
		}
		LOGGER.finer("Eden Space Util = " + edenSpaceUtil + "%");

		// update survivor space util
		long survivorSpaceUsed = 0;
		try {
			survivorSpaceUsed = memoryPoolMxBeanHelper.getSurvivorCollectionUsed();
		} catch (Exception e) {
			LOGGER.fine("WARNING: Unable to retrieve survivor collection used from MemoryPool MBean due to: " + e.getMessage() );
		}
		long survivorSpaceCommitted = 0;
		try {
			survivorSpaceCommitted = memoryPoolMxBeanHelper.getSurvivorCollectionCommitted();
		} catch (Exception e) {
			LOGGER.fine("WARNING: Unable to retrieve survivor collection committed from MemoryPool MBean due to: " + e.getMessage());
		}
		LOGGER.finer("Survivor Collection Used = " + survivorSpaceUsed);
		LOGGER.finer("Survivor Collection Committed = " + survivorSpaceCommitted);
		if (survivorSpaceCommitted > 0) {
			this.survivorSpaceUtil = calculateHeapUtil( survivorSpaceUsed, survivorSpaceCommitted );
		}
		LOGGER.fine("Survivor Space Util = " + survivorSpaceUtil + "%");
		
		// update tenured heap util
		long tenuredHeapUsed = 0;
		try {
			tenuredHeapUsed = memoryPoolMxBeanHelper.getTenuredCollectionUsed();
		} catch (Exception e) {
			LOGGER.fine("WARNING: Unable to retrieve tenured collection used from MemoryPool MBean due to: " + e.getMessage());
		}
		long tenuredHeapCommitted = 0;
		try {
			tenuredHeapCommitted = memoryPoolMxBeanHelper.getTenuredCollectionCommitted();
		} catch (Exception e) {
			LOGGER.fine("WARNING: Unable to retrieve tenured collection committed from MemoryPool MBean due to: " + e.getMessage());
		}
		LOGGER.finer("Tenured Collection Used = " + tenuredHeapUsed);
		LOGGER.finer("Tenured Collection Committed = " + tenuredHeapCommitted);
		if (tenuredHeapCommitted > 0) {
			if (tenuredHeapUsed == 0) {
				// either the tenured area is empty or there hasn't been a tenured GC - revert to current heap used
				try {
					tenuredHeapUsed = memoryPoolMxBeanHelper.getTenuredHeapUsed();
				} catch (Exception e) {
					LOGGER.fine("WARNING: Unable to retrieve tenured heap used from MemoryPool MBean due to: " + e.getMessage());
				}
				LOGGER.finer("Tenured Heap Used = " + tenuredHeapUsed);
			}
			this.tenuredHeapUtil = calculateHeapUtil( tenuredHeapUsed, tenuredHeapCommitted );
		}
		LOGGER.fine("Tenured Heap Util = " + tenuredHeapUtil + "%");
    	
    }

	public double getEdenSpaceUtil() {
		return edenSpaceUtil;
	}

	public void setEdenSpaceUtil(double edenSpaceUtil) {
		this.edenSpaceUtil = edenSpaceUtil;
	}

	public double getSurvivorSpaceUtil() {
		return survivorSpaceUtil;
	}

	public void setSurvivorSpaceUtil(double survivorSpaceUtil) {
		this.survivorSpaceUtil = survivorSpaceUtil;
	}

	public double getTenuredHeapUtil() {
		return tenuredHeapUtil;
	}

	public void setTenuredHeapUtil(double tenuredHeapUtil) {
		this.tenuredHeapUtil = tenuredHeapUtil;
	}
	
	private double calculateHeapUtil ( long heapUsed, long heapCommitted ) {
		
		return ( (double) heapUsed / heapCommitted ) * 100;
		
	}

}
