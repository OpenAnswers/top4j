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

package io.top4j.javaagent.mbeans.jvm.gc;

import io.top4j.javaagent.exception.MBeanInitException;

import javax.management.MBeanServerConnection;
import java.util.logging.Logger;

public class GCPauseTime {
	
	volatile private double meanNurseryGCTime;
	volatile private double meanTenuredGCTime;
	private GCTimeBean gcTimeBean;
	private GarbageCollectorMXBeanHelper gcMXBeanHelper;
	
	private static final Logger LOGGER = Logger.getLogger(GCPauseTime.class.getName());
	
	public GCPauseTime ( MBeanServerConnection mbsc ) throws Exception {

		LOGGER.fine("Initialising GC Pause Time....");
		
		// instantiate new GarbageCollectorMXBeanHelper and GCTimeBean
		try {
			this.gcMXBeanHelper = new GarbageCollectorMXBeanHelper( mbsc );
			this.gcTimeBean = new GCTimeBean( mbsc );
		} catch (Exception e) {
			throw new MBeanInitException( e, "Failed to initialise GC Pause Time stats collector due to: " + e.getMessage() );
		}

	}
	
	/** Update GC Pause Time. */
    public void update( ) {
    	
    	long nurseryGCCount = 0;
    	long nurseryGCTime = 0;
    	long tenuredGCCount = 0;
    	long tenuredGCTime = 0;
    	long intervalNurseryGCTime;
    	long intervalNurseryGCCount;
    	long intervalTenuredGCTime;
    	long intervalTenuredGCCount;
    	
		try {
			// calculate mean nursery GC pause time
			nurseryGCTime = gcMXBeanHelper.getNurseryGCTime();
			intervalNurseryGCTime = nurseryGCTime - gcTimeBean.getLastNurseryGCTime();
			nurseryGCCount = gcMXBeanHelper.getNurseryGCCount();
			intervalNurseryGCCount = nurseryGCCount - gcTimeBean.getLastNurseryGCCount();
			if (intervalNurseryGCCount > 0 ) {
				meanNurseryGCTime = (double) intervalNurseryGCTime / intervalNurseryGCCount;
			}
			LOGGER.fine("Mean Nursery GC Time = " + meanNurseryGCTime + "ms");
		} catch (Exception e) {
			LOGGER.fine("WARNING: Unable to update nursery GC pause time due to: " + e.getMessage() );
		}
    	gcTimeBean.setLastNurseryGCTime(nurseryGCTime);
    	gcTimeBean.setLastNurseryGCCount(nurseryGCCount);
    	
		try {
			// calculate mean tenured GC pause time
			tenuredGCTime = gcMXBeanHelper.getTenuredGCTime();
			intervalTenuredGCTime = tenuredGCTime - gcTimeBean.getLastTenuredGCTime();
			tenuredGCCount = gcMXBeanHelper.getTenuredGCCount();
			intervalTenuredGCCount = tenuredGCCount - gcTimeBean.getLastTenuredGCCount();
			if (intervalTenuredGCCount > 0) {
				meanTenuredGCTime = (double) intervalTenuredGCTime / intervalTenuredGCCount;
			}
			LOGGER.fine("Mean Tenured GC Time = " + meanTenuredGCTime + "ms");
		} catch (Exception e) {
			LOGGER.fine("WARNING: Unable to update tenured GC pause time due to: " + e.getMessage() );
		}
    	gcTimeBean.setLastTenuredGCTime(tenuredGCTime);
    	gcTimeBean.setLastTenuredGCCount(tenuredGCCount);
    	
    }

	public double getMeanNurseryGCTime() {
		return meanNurseryGCTime;
	}

	public void setMeanNurseryGCTime(double meanNurseryGCTime) {
		this.meanNurseryGCTime = meanNurseryGCTime;
	}

	public double getMeanTenuredGCTime() {
		return meanTenuredGCTime;
	}

	public void setMeanTenuredGCTime(double meanTenuredGCTime) {
		this.meanTenuredGCTime = meanTenuredGCTime;
	}

}
