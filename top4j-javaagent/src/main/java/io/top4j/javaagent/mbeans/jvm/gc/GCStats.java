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

import io.top4j.javaagent.profiler.CpuTime;

import javax.management.MBeanServerConnection;
import java.util.logging.*;


public class GCStats implements GCStatsMXBean {

    private GCOverhead gcOverhead;
    private GCPauseTime gcPauseTime;
    private CpuTime cpuTime = new CpuTime();
    private double mBeanCpuTime;
    private boolean enabled = true;
    private String failureReason;

    private static final Logger LOGGER = Logger.getLogger(GCStats.class.getName());

    public GCStats(MBeanServerConnection mbsc) throws Exception {

        LOGGER.fine("Initialising GC stats....");

        // instantiate new GC Overhead
        GCOverhead gcOverhead = new GCOverhead(mbsc);

        // instantiate new GC Pause Time
        GCPauseTime gcPauseTime = new GCPauseTime(mbsc);

        this.gcOverhead = gcOverhead;
        this.gcPauseTime = gcPauseTime;

    }

    /**
     * Update GC stats.
     */
    public synchronized void update() {

        if (enabled) {
            try {
                // update GC stats
                updateGCStats();
            } catch (Exception e) {
                // something went wrong - record failure reason and disable any further updates
                this.failureReason = e.getMessage();
                this.enabled = false;
                LOGGER.severe("TOP4J ERROR: Failed to update GCStats MBean due to: " + e.getMessage());
                LOGGER.severe("TOP4J ERROR: Further GCStats MBean updates will be disabled from now on.");
            }
        }

    }

    private synchronized void updateGCStats() {

        // initialise thread CPU timer
        cpuTime.init();

        LOGGER.fine("Updating GC stats....");

        // update GC Overhead
        gcOverhead.update();

        // update GC pause time stats
        gcPauseTime.update();

        // update GC stats CPU time
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
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean getEnabled() {
        return this.enabled;
    }

    @Override
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    @Override
    public String getFailureReason() {
        return this.failureReason;
    }

    @Override
    public void setGcOverhead(double gcOverhead) {
        this.gcOverhead.setGcOverhead(gcOverhead);

    }

    @Override
    public double getGcOverhead() {
        return this.gcOverhead.getGcOverhead();
    }

    @Override
    public void setMeanNurseryGCTime(double meanNurseryGCTime) {
        this.gcPauseTime.setMeanNurseryGCTime(meanNurseryGCTime);
    }

    @Override
    public double getMeanNurseryGCTime() {
        return this.gcPauseTime.getMeanNurseryGCTime();
    }

    @Override
    public void setMeanTenuredGCTime(double meanTenuredGCTime) {
        gcPauseTime.setMeanTenuredGCTime(meanTenuredGCTime);
    }

    @Override
    public double getMeanTenuredGCTime() {
        return this.gcPauseTime.getMeanTenuredGCTime();
    }

}
