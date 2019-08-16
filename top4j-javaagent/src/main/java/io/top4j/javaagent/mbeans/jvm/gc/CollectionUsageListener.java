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

package io.top4j.javaagent.mbeans.jvm.gc;

import java.lang.management.MemoryNotificationInfo;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.Notification;

import io.top4j.javaagent.mbeans.jvm.memory.MemoryPoolMXBeanHelper;
import io.top4j.javaagent.mbeans.jvm.memory.MemorySurvivorBean;

public class CollectionUsageListener implements javax.management.NotificationListener {

	private MemoryPoolMXBeanHelper memoryPoolMxBeanHelper;
	private GarbageCollectorMXBeanHelper gcMXBeanHelper;
	private MemorySurvivorBean memorySurvivor;
	
	private static final Logger LOGGER = Logger.getLogger(CollectionUsageListener.class.getName());
	
	public CollectionUsageListener( MBeanServerConnection mbsc, MemorySurvivorBean memorySurvivor ) throws Exception {
		
		this.memorySurvivor = memorySurvivor;
		
		// instantiate new MemoryPoolMXBeanHelper
		try {
			this.memoryPoolMxBeanHelper = new MemoryPoolMXBeanHelper( mbsc );
		} catch (Exception e) {
			throw new Exception( "Failed to initialise Collection Usage Listener due to: " + e.getMessage() );
		}

		// instantiate new GarbageCollectorMXBeanHelper
		try {
			this.gcMXBeanHelper = new GarbageCollectorMXBeanHelper( mbsc );
		} catch (Exception e) {
			throw new Exception( "Failed to initialise Collection Usage Listener due to: " + e.getMessage() );
		}
	}
	
	@Override
	public void handleNotification(Notification notification, Object handback) {
		
		String notifType = notification.getType();
        if (notifType.equals(MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED)) {
            LOGGER.fine("MEMORY_COLLECTION_THRESHOLD_EXCEEDED");
			long survivorCollectionUsed = 0;
			try {
				survivorCollectionUsed = memoryPoolMxBeanHelper.getSurvivorCollectionUsed();
			} catch (Exception e) {
				LOGGER.fine("Unable to retrieve Survivor Collection Used.");
			}
			memorySurvivor.addSurvivors(survivorCollectionUsed);
            LOGGER.fine("Survivor Collection Used = " + Long.valueOf(survivorCollectionUsed).toString());
			try {
				LOGGER.fine("Survivor GC Count = " + gcMXBeanHelper.getNurseryGCCount());
			} catch (Exception e) {
				LOGGER.fine("Unable to retrieve Survivor GC Count.");
			}
		}
		
	}

}
