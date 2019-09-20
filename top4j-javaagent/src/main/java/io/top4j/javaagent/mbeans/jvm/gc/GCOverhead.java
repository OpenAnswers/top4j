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

public class GCOverhead {

    volatile private double gcOverhead;
    private GCTimeBean gcTimeBean;
    private GarbageCollectorMXBeanHelper gcMXBeanHelper;

    private static final Logger LOGGER = Logger.getLogger(GCOverhead.class.getName());

    public GCOverhead(MBeanServerConnection mbsc) throws Exception {

        LOGGER.fine("Initialising GC Overhead....");

        // instantiate new GarbageCollectorMXBeanHelper and GCTimeBean
        try {
            this.gcMXBeanHelper = new GarbageCollectorMXBeanHelper(mbsc);
            this.gcTimeBean = new GCTimeBean(mbsc);
        } catch (Exception e) {
            throw new MBeanInitException(e, "Failed to initialise GC Overhead stats collector due to: " + e.getMessage());
        }

    }

    /**
     * Update GC Overhead.
     */
    public void update() {

        long systemTime = System.currentTimeMillis();
        long gcTime;
        long intervalGCTime;
        long intervalSystemTime;

        gcTime = gcMXBeanHelper.getGCTime();
        intervalGCTime = gcTime - gcTimeBean.getLastGCTime();
        intervalSystemTime = systemTime - gcTimeBean.getLastSystemTime();
        LOGGER.finer("GC Overhead Interval GC Time = " + intervalGCTime);
        LOGGER.finer("GC Overhead Interval System Time = " + intervalSystemTime);
        gcOverhead = calculateGCOverhead(intervalGCTime, intervalSystemTime);
        LOGGER.fine("GC Overhead = " + gcOverhead + "%");
        gcTimeBean.setLastGCTime(gcTime);
        gcTimeBean.setLastSystemTime(systemTime);

    }

    public double getGcOverhead() {
        return gcOverhead;
    }

    public void setGcOverhead(double gcOverhead) {
        this.gcOverhead = gcOverhead;
    }

    private double calculateGCOverhead(long intervalGCTime, long intervalSystemTime) {

        return ((double) intervalGCTime / intervalSystemTime) * 100;

    }

}
