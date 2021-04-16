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

import java.util.logging.*;

import javax.management.MBeanServerConnection;
import javax.management.NotificationEmitter;

import io.top4j.javaagent.listener.CollectionListener;
import io.top4j.javaagent.profiler.CpuTime;

public class MemoryStats implements MemoryStatsMXBean {

    private MemoryPoolAllocationRate memoryAllocationRate;
    private MemoryPoolAllocationRate memorySurvivorRate;
    private MemoryPoolAllocationRate memoryPromotionRate;
    private CpuTime cpuTime = new CpuTime();
    private double mBeanCpuTime;
    private boolean enabled = true;
    private String failureReason;
    private boolean onegen = false;

    private static final Logger LOGGER = Logger.getLogger(MemoryStats.class.getName());

    public MemoryStats(MBeanServerConnection mbsc) throws Exception {

        LOGGER.fine("Initialising Memory stats....");

        // instantiate new MemoryPoolUsageTracker to track nursery pool usage
        MemoryPoolUsageTracker nurseryPoolUsageTracker = new MemoryPoolUsageTracker(mbsc, "Nursery");
        this.onegen = nurseryPoolUsageTracker.getMemoryPoolName().equals("ZHeap");

        // instantiate new MemoryPoolAllocationRate to store memory allocation rate
        MemoryPoolAllocationRate memoryAllocationRate = new MemoryPoolAllocationRate(mbsc, "Nursery", nurseryPoolUsageTracker);

        MemoryPoolUsageTracker survivorPoolUsageTracker = null, tenuredPoolUsageTracker = null;
        MemoryPoolAllocationRate memorySurvivorRate = null, memoryPromotionRate = null;
        if (!onegen) {
            // instantiate new MemoryPoolUsageTracker to track survivor pool usage
            survivorPoolUsageTracker = new MemoryPoolUsageTracker(mbsc, "Survivor");

            // instantiate new MemoryPoolUsageTracker to track tenured pool usage
            tenuredPoolUsageTracker = new MemoryPoolUsageTracker(mbsc, "Tenured");


            // instantiate new MemorySurvivorRate to store memory survivor rate
            memorySurvivorRate = new MemoryPoolAllocationRate(mbsc, "Survivor", survivorPoolUsageTracker);

            // instantiate new MemoryPoolAllocationRate to store memory promotion rate
            memoryPromotionRate = new MemoryPoolAllocationRate(mbsc, "Tenured", tenuredPoolUsageTracker);
        }
        // instantiate new MemoryPoolMXBeanHelper
        MemoryPoolMXBeanHelper memoryPoolMxBeanHelper = new MemoryPoolMXBeanHelper(mbsc);

        // register CollectionListener with MemoryMXBean
        MemoryMXBean memoryMXBean = ManagementFactory.getPlatformMXBean(mbsc, MemoryMXBean.class);
        NotificationEmitter emitter = (NotificationEmitter) memoryMXBean;
        CollectionListener listener = new CollectionListener(mbsc, nurseryPoolUsageTracker, survivorPoolUsageTracker, tenuredPoolUsageTracker);
        emitter.addNotificationListener(listener, null, null);

        // set memory pool usage thresholds
        int memoryUsageThreshold = 1;
        if (!onegen)
            memoryPoolMxBeanHelper.setTenuredUsageThreshold(memoryUsageThreshold);

        // set memory pool collection usage thresholds
        int collectionUsageThreshold = 1;
        memoryPoolMxBeanHelper.setNurseryCollectionUsageThreshold(collectionUsageThreshold);
        if (!onegen) {
            memoryPoolMxBeanHelper.setSurvivorCollectionUsageThreshold(collectionUsageThreshold);
            memoryPoolMxBeanHelper.setTenuredCollectionUsageThreshold(collectionUsageThreshold);
        }

        this.memoryAllocationRate = memoryAllocationRate;
        this.memorySurvivorRate = memorySurvivorRate;
        this.memoryPromotionRate = memoryPromotionRate;

    }

    /**
     * Update Memory stats.
     */
    public synchronized void update() {

        if (enabled) {
            try {
                // update memory stats
                updateMemoryStats();
            } catch (Exception e) {
                // something went wrong - record failure reason and disable any further updates
                this.failureReason = e.getMessage();
                this.enabled = false;
                LOGGER.severe("TOP4J ERROR: Failed to update MemoryStats MBean due to: " + e.getMessage());
                LOGGER.severe("TOP4J ERROR: Further MemoryStats MBean updates will be disabled from now on.");
            }
        }
    }

    private synchronized void updateMemoryStats() {

        // initialise thread CPU timer
        cpuTime.init();

        LOGGER.fine("Updating Memory stats....");

        // update memory allocation rate
        this.memoryAllocationRate.update();

        if (!onegen) {
            // update memory survivor rate
            this.memorySurvivorRate.update();

            // update memory promotion rate
            this.memoryPromotionRate.update();
        }

        // update memory stats CPU time
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
    public void setMemoryAllocationRate(double memoryAllocationRate) {
        this.memoryAllocationRate.setMemoryPoolAllocationRate(memoryAllocationRate);
    }

    @Override
    public double getMemoryAllocationRate() {
        return this.memoryAllocationRate.getMemoryPoolAllocationRate();
    }

    @Override
    public void setMemorySurvivorRate(double memorySurvivorRate) {
        this.memorySurvivorRate.setMemoryPoolAllocationRate(memorySurvivorRate);
    }

    @Override
    public double getMemorySurvivorRate() {
        if (this.memorySurvivorRate == null)
            return 0.0;
        return this.memorySurvivorRate.getMemoryPoolAllocationRate();
    }

    @Override
    public void setMemoryPromotionRate(double memoryPromotionRate) {
        this.memoryPromotionRate.setMemoryPoolAllocationRate(memoryPromotionRate);
    }

    @Override
    public double getMemoryPromotionRate() {
        if (this.memoryPromotionRate == null)
            return 0.0;
        return this.memoryPromotionRate.getMemoryPoolAllocationRate();
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

}
