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

package io.top4j.javaagent.listener;

import java.lang.management.MemoryNotificationInfo;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.openmbean.CompositeData;

import io.top4j.javaagent.exception.MBeanInitException;
import io.top4j.javaagent.mbeans.jvm.gc.GarbageCollectorMXBeanHelper;
import io.top4j.javaagent.mbeans.jvm.memory.MemoryPoolMXBeanHelper;
import io.top4j.javaagent.mbeans.jvm.memory.MemoryPoolUsageTracker;

public class CollectionListener implements javax.management.NotificationListener {

    private GarbageCollectorMXBeanHelper gcMXBeanHelper;
    private MemoryPoolUsageTracker nurseryPoolUsageTracker;
    private MemoryPoolUsageTracker survivorPoolUsageTracker;
    private MemoryPoolUsageTracker tenuredPoolUsageTracker;
    private String survivorPoolName;
    private String tenuredPoolName;

    private static final Logger LOGGER = Logger.getLogger(CollectionListener.class.getName());

    public CollectionListener(MBeanServerConnection mbsc,
                              MemoryPoolUsageTracker nurseryPoolUsageTracker,
                              MemoryPoolUsageTracker survivorPoolUsageTracker,
                              MemoryPoolUsageTracker tenuredPoolUsageTracker) throws Exception {

        this.nurseryPoolUsageTracker = nurseryPoolUsageTracker;
        this.survivorPoolUsageTracker = survivorPoolUsageTracker;
        this.tenuredPoolUsageTracker = tenuredPoolUsageTracker;

        // instantiate new MemoryPoolMXBeanHelper
        MemoryPoolMXBeanHelper memoryPoolMxBeanHelper = new MemoryPoolMXBeanHelper(mbsc);

        // instantiate new GarbageCollectorMXBeanHelper
        try {
            this.gcMXBeanHelper = new GarbageCollectorMXBeanHelper(mbsc);
        } catch (Exception e) {
            throw new MBeanInitException(e, "Failed to initialise Collection Listener due to: " + e.getMessage());
        }

        // get survivor pool name
        this.survivorPoolName = memoryPoolMxBeanHelper.getSurvivorSpacePoolName();

        // get tenured pool name
        this.tenuredPoolName = memoryPoolMxBeanHelper.getTenuredPoolName();

    }

    @Override
    public void handleNotification(Notification notification, Object handback) {

        String notifType = notification.getType();
        if (notifType.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED) ||
                notifType.equals(MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED)) {
            LOGGER.finer("MEMORY_THRESHOLD_EXCEEDED");

            // retrieve the memory notification information
            CompositeData cd = (CompositeData) notification.getUserData();
            MemoryNotificationInfo info = MemoryNotificationInfo.from(cd);
            String poolName = info.getPoolName();

            if (poolName.equals(survivorPoolName)) {

                try {
                    LOGGER.finer("Collection Listener Nursery GC Count = " + gcMXBeanHelper.getNurseryGCCount());
                } catch (Exception e) {
                    LOGGER.fine("Unable to retrieve Nursery GC Count");
                }
                // update nursery pool usage tracker
                nurseryPoolUsageTracker.update();
                LOGGER.finer("Collection Listener Nursery Pool Usage = " + nurseryPoolUsageTracker.getMemoryPoolIntervalUsage());

                try {
                    LOGGER.finer("Collection Listener Survivor GC Count = " + gcMXBeanHelper.getNurseryGCCount());
                } catch (Exception e) {
                    LOGGER.fine("Unable to retrieve Survivor GC Count");
                }
                // update survivor pool usage tracker
                survivorPoolUsageTracker.update();
                LOGGER.finer("Collection Listener Survivor Pool Usage = " + survivorPoolUsageTracker.getMemoryPoolIntervalUsage());

            }

            if (poolName.equals(tenuredPoolName)) {

                try {
                    LOGGER.finer("Collection Listener Tenured GC Count = " + gcMXBeanHelper.getTenuredGCCount());
                } catch (Exception e) {
                    LOGGER.fine("Unable to retrieve Tenured GC Count");
                }
                // update tenured pool usage tracker
                tenuredPoolUsageTracker.update();
                LOGGER.finer("Tenured Pool Usage = " + tenuredPoolUsageTracker.getMemoryPoolIntervalUsage());

            }

        }

    }

}
