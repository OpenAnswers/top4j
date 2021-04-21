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

import io.top4j.javaagent.exception.MBeanDiscoveryException;
import io.top4j.javaagent.exception.MBeanInitException;
import io.top4j.javaagent.exception.MBeanRuntimeException;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.management.*;
import javax.management.openmbean.CompositeData;

public class MemoryPoolMXBeanHelper {

    private String nurseryPoolName;
    private String survivorSpacePoolName;
    private String tenuredPoolName;
    private ObjectName nurseryPoolObjectName;
    private ObjectName survivorSpacePoolObjectName;
    private ObjectName tenuredPoolObjectName;
    private List<MemoryPoolMXBean> memPoolMXBeans;
    private MBeanServerConnection mbsc;
    private boolean onegen = false; // single-generation heap?

    private static final Logger LOGGER = Logger.getLogger(MemoryPoolMXBeanHelper.class.getName());

    public MemoryPoolMXBeanHelper(MBeanServerConnection mbsc) throws MBeanInitException {

        // store MBean server connection
        this.mbsc = mbsc;
        // get and store list of MemoryPoolMXBean
        try {
            this.memPoolMXBeans = ManagementFactory.getPlatformMXBeans(mbsc, MemoryPoolMXBean.class);
        } catch (IOException e) {
            throw new MBeanInitException(e, "JMX IOException: " + e.getMessage());
        }

        try {
            // discover nursery Pool name
            this.setNurseryPoolName(this.discoverNurseryPoolName());
            this.nurseryPoolObjectName = new ObjectName("java.lang:type=MemoryPool,name=" + nurseryPoolName);

            this.onegen = this.nurseryPoolName.equals("ZHeap");

            if (!onegen) {
                // discover survivor space Pool name
                this.setSurvivorSpacePoolName(this.discoverSurvivorSpacePoolName());
                // discover tenured Pool name
                this.setTenuredPoolName(this.discoverTenuredPoolName());
                this.survivorSpacePoolObjectName = new ObjectName("java.lang:type=MemoryPool,name=" + survivorSpacePoolName);
                this.tenuredPoolObjectName = new ObjectName("java.lang:type=MemoryPool,name=" + tenuredPoolName);
            }

        } catch (MalformedObjectNameException e) {
            throw new MBeanInitException(e, "JMX MalformedObjectNameException: " + e.getMessage());
        }

    }

    public String getNurseryPoolName() {
        return nurseryPoolName;
    }

    public void setNurseryPoolName(String nurseryPoolName) {
        this.nurseryPoolName = nurseryPoolName;
    }

    public String getSurvivorSpacePoolName() {
        return survivorSpacePoolName;
    }

    public void setSurvivorSpacePoolName(String survivorSpacePoolName) {
        this.survivorSpacePoolName = survivorSpacePoolName;
    }

    public String getTenuredPoolName() {
        return tenuredPoolName;
    }

    public void setTenuredPoolName(String tenuredPoolName) {
        this.tenuredPoolName = tenuredPoolName;
    }

    /**
     * Lists all available memory pool names
     * @return list of memory pool names
     */
    public List<String> listMemoryPoolNames() {

        List<String> memoryPoolNames = new ArrayList<>();

        for (MemoryPoolMXBean memPoolMXBean : memPoolMXBeans) {

            String mpName = memPoolMXBean.getName();
            LOGGER.finest("Memory Pool MX Bean Name = " + mpName);

            // add mpName to list of memoryPoolNames
            memoryPoolNames.add(mpName);
        }

        return memoryPoolNames;

    }

    private String discoverNurseryPoolName() throws MBeanDiscoveryException {

        String poolName = null;

        for (MemoryPoolMXBean memPoolMXBean : memPoolMXBeans) {

            String mpName = memPoolMXBean.getName();
            LOGGER.finest("Memory Pool MX Bean Name = " + mpName);

            if (mpName.endsWith("Eden Space") ||
                    mpName.equals("Nursery") ||
                    mpName.equals("ZHeap")) {
                poolName = mpName;
            }

        }
        LOGGER.fine("Nursery Pool Name = " + poolName);
        if (poolName == null) {
            throw new MBeanDiscoveryException("Unable to auto discover nursery pool name.");
        }
        return poolName;

    }

    private String discoverSurvivorSpacePoolName() throws MBeanDiscoveryException {

        String poolName = null;

        for (MemoryPoolMXBean memPoolMXBean : memPoolMXBeans) {

            String mpName = memPoolMXBean.getName();
            LOGGER.finest("Memory Pool MX Bean Name = " + mpName);

            if (mpName.endsWith("Survivor Space")) {
                poolName = mpName;
            }

        }
        LOGGER.fine("Survivor Space Pool Name = " + poolName);
        if (nurseryPoolName == null) {
            throw new MBeanDiscoveryException("Unable to auto discover survivor space pool name.");
        }
        return poolName;

    }

    private String discoverTenuredPoolName() throws MBeanDiscoveryException {

        String poolName = null;

        for (MemoryPoolMXBean memPoolMXBean : memPoolMXBeans) {

            String mpName = memPoolMXBean.getName();
            LOGGER.finest("Memory Pool MX Bean Name = " + mpName);

            if (mpName.equals("Tenured Gen") ||
                    mpName.endsWith("Old Gen") ||
                    mpName.equals("Old Space")) {
                poolName = mpName;
            }

        }
        LOGGER.fine("Tenured Pool Name = " + poolName);
        if (nurseryPoolName == null) {
            throw new MBeanDiscoveryException("Unable to auto discover tenured pool name.");
        }
        return poolName;

    }

    public long getNurseryHeapUsed() throws MBeanRuntimeException {

        long nurseryHeapUsed;

        nurseryHeapUsed = getHeapUsed(nurseryPoolObjectName);

        return nurseryHeapUsed;

    }

    public long getSurvivorSpaceUsed() throws MBeanRuntimeException {

        long survivorSpaceUsed;

        survivorSpaceUsed = getHeapUsed(survivorSpacePoolObjectName);

        return survivorSpaceUsed;

    }

    public long getTenuredHeapUsed() throws MBeanRuntimeException {

        long tenuredHeapUsed;

        tenuredHeapUsed = getHeapUsed(tenuredPoolObjectName);

        return tenuredHeapUsed;

    }

    private long getHeapUsed(ObjectName objectName) throws MBeanRuntimeException {

        long heapUsed = 0;

        try {
            MemoryUsage memoryUsage = MemoryUsage.from((CompositeData) mbsc.getAttribute(objectName, "Usage"));
            heapUsed = (long) memoryUsage.getUsed();
        } catch (AttributeNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX AttributeNotFoundException: " + e.getMessage());
        } catch (InstanceNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX InstanceNotFoundException: " + e.getMessage());
        } catch (MBeanException e) {
            throw new MBeanRuntimeException(e, "JMX MBeanException: " + e.getMessage());
        } catch (ReflectionException e) {
            throw new MBeanRuntimeException(e, "JMX ReflectionException: " + e.getMessage());
        } catch (IOException e) {
            throw new MBeanRuntimeException(e, "JMX IOException: " + e.getMessage());
        }

        return heapUsed;
    }

    public long getNurseryHeapCommitted() throws MBeanRuntimeException {

        long nurseryHeapCommitted;

        nurseryHeapCommitted = getHeapCommitted(nurseryPoolObjectName);

        return nurseryHeapCommitted;

    }

    public long getSurvivorSpaceCommitted() throws MBeanRuntimeException {

        long survivorSpaceCommitted;

        survivorSpaceCommitted = getHeapCommitted(survivorSpacePoolObjectName);

        return survivorSpaceCommitted;

    }

    public long getTenuredHeapCommitted() throws MBeanRuntimeException {

        long tenuredHeapCommitted;

        tenuredHeapCommitted = getHeapCommitted(tenuredPoolObjectName);

        return tenuredHeapCommitted;

    }

    private long getHeapCommitted(ObjectName objectName) throws MBeanRuntimeException {

        long heapCommitted = 0;

        try {
            MemoryUsage memoryUsage = MemoryUsage.from((CompositeData) mbsc.getAttribute(objectName, "Usage"));
            heapCommitted = (long) memoryUsage.getCommitted();
        } catch (AttributeNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX AttributeNotFoundException: " + e.getMessage());
        } catch (InstanceNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX InstanceNotFoundException: " + e.getMessage());
        } catch (MBeanException e) {
            throw new MBeanRuntimeException(e, "JMX MBeanException: " + e.getMessage());
        } catch (ReflectionException e) {
            throw new MBeanRuntimeException(e, "JMX ReflectionException: " + e.getMessage());
        } catch (IOException e) {
            throw new MBeanRuntimeException(e, "JMX IOException: " + e.getMessage());
        }

        return heapCommitted;
    }

    public long getNurseryCollectionUsed() throws MBeanRuntimeException {

        long nurseryCollectionUsed;

        nurseryCollectionUsed = getCollectionUsed(nurseryPoolObjectName);

        return nurseryCollectionUsed;

    }

    public long getSurvivorCollectionUsed() throws MBeanRuntimeException {

        long survivorCollectionUsed;

        survivorCollectionUsed = getCollectionUsed(survivorSpacePoolObjectName);

        return survivorCollectionUsed;

    }

    public long getTenuredCollectionUsed() throws MBeanRuntimeException {

        long tenuredCollectionUsed;

        tenuredCollectionUsed = getCollectionUsed(tenuredPoolObjectName);

        return tenuredCollectionUsed;

    }

    private long getCollectionUsed(ObjectName objectName) throws MBeanRuntimeException {

        long collectionUsed = 0;

        try {
            MemoryUsage memoryUsage = MemoryUsage.from((CompositeData) mbsc.getAttribute(objectName, "CollectionUsage"));
            collectionUsed = (long) memoryUsage.getUsed();
        } catch (AttributeNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX AttributeNotFoundException: " + e.getMessage());
        } catch (InstanceNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX InstanceNotFoundException: " + e.getMessage());
        } catch (MBeanException e) {
            throw new MBeanRuntimeException(e, "JMX MBeanException: " + e.getMessage());
        } catch (ReflectionException e) {
            throw new MBeanRuntimeException(e, "JMX ReflectionException: " + e.getMessage());
        } catch (IOException e) {
            throw new MBeanRuntimeException(e, "JMX IOException: " + e.getMessage());
        }

        return collectionUsed;
    }

    public long getNurseryCollectionCommitted() throws MBeanRuntimeException {

        long nurseryCollectionCommitted;

        nurseryCollectionCommitted = getCollectionCommitted(nurseryPoolObjectName);

        return nurseryCollectionCommitted;

    }

    public long getSurvivorCollectionCommitted() throws MBeanRuntimeException {

        long survivorCollectionCommitted;

        survivorCollectionCommitted = getCollectionCommitted(survivorSpacePoolObjectName);

        return survivorCollectionCommitted;

    }

    public long getTenuredCollectionCommitted() throws MBeanRuntimeException {

        long tenuredCollectionCommitted;

        tenuredCollectionCommitted = getCollectionCommitted(tenuredPoolObjectName);

        return tenuredCollectionCommitted;

    }

    private long getCollectionCommitted(ObjectName objectName) throws MBeanRuntimeException {

        long collectionCommitted = 0;

        try {
            MemoryUsage collectionUsage = MemoryUsage.from((CompositeData) mbsc.getAttribute(objectName, "CollectionUsage"));
            collectionCommitted = (long) collectionUsage.getCommitted();
        } catch (AttributeNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX AttributeNotFoundException: " + e.getMessage());
        } catch (InstanceNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX InstanceNotFoundException: " + e.getMessage());
        } catch (MBeanException e) {
            throw new MBeanRuntimeException(e, "JMX MBeanException: " + e.getMessage());
        } catch (ReflectionException e) {
            throw new MBeanRuntimeException(e, "JMX ReflectionException: " + e.getMessage());
        } catch (IOException e) {
            throw new MBeanRuntimeException(e, "JMX IOException: " + e.getMessage());
        }

        return collectionCommitted;
    }

    public void setNurseryCollectionUsageThreshold(long threshold) throws MBeanRuntimeException {

        setCollectionUsageThreshold(nurseryPoolObjectName, threshold);

    }

    public void setSurvivorCollectionUsageThreshold(long threshold) throws MBeanRuntimeException {

        setCollectionUsageThreshold(survivorSpacePoolObjectName, threshold);

    }

    public void setTenuredCollectionUsageThreshold(long threshold) throws MBeanRuntimeException {

        setCollectionUsageThreshold(tenuredPoolObjectName, threshold);

    }

    private void setCollectionUsageThreshold(ObjectName objectName, long threshold) throws MBeanRuntimeException {

        Attribute collectionUsageThreshold = new Attribute("CollectionUsageThreshold", threshold);
        try {
            mbsc.setAttribute(objectName, collectionUsageThreshold);
        } catch (InstanceNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX InstanceNotFoundException: " + e.getMessage());
        } catch (InvalidAttributeValueException e) {
            throw new MBeanRuntimeException(e, "JMX InvalidAttributeValueException: " + e.getMessage());
        } catch (AttributeNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX AttributeNotFoundException: " + e.getMessage());
        } catch (ReflectionException e) {
            throw new MBeanRuntimeException(e, "JMX ReflectionException: " + e.getMessage());
        } catch (MBeanException e) {
            throw new MBeanRuntimeException(e, "JMX MBeanException: " + e.getMessage());
        } catch (IOException e) {
            throw new MBeanRuntimeException(e, "JMX IOException: " + e.getMessage());
        }

    }

    public void setNurseryUsageThreshold(long threshold) throws MBeanRuntimeException {

        setUsageThreshold(nurseryPoolObjectName, threshold);

    }

    public void setSurvivorUsageThreshold(long threshold) throws MBeanRuntimeException {

        setUsageThreshold(survivorSpacePoolObjectName, threshold);

    }

    public void setTenuredUsageThreshold(long threshold) throws MBeanRuntimeException {

        setUsageThreshold(tenuredPoolObjectName, threshold);

    }

    private void setUsageThreshold(ObjectName objectName, long threshold) throws MBeanRuntimeException {

        Attribute usageThreshold = new Attribute("UsageThreshold", threshold);
        try {
            mbsc.setAttribute(objectName, usageThreshold);
        } catch (InstanceNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX InstanceNotFoundException: " + e.getMessage());
        } catch (InvalidAttributeValueException e) {
            throw new MBeanRuntimeException(e, "JMX InvalidAttributeValueException: " + e.getMessage());
        } catch (AttributeNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX AttributeNotFoundException: " + e.getMessage());
        } catch (ReflectionException e) {
            throw new MBeanRuntimeException(e, "JMX ReflectionException: " + e.getMessage());
        } catch (MBeanException e) {
            throw new MBeanRuntimeException(e, "JMX MBeanException: " + e.getMessage());
        } catch (IOException e) {
            throw new MBeanRuntimeException(e, "JMX IOException: " + e.getMessage());
        }

    }

    public long getNurseryPeakUsed() throws MBeanRuntimeException {

        long nurseryPeakUsed;

        nurseryPeakUsed = getPeakUsed(nurseryPoolObjectName);

        return nurseryPeakUsed;

    }

    public long getSurvivorPeakUsed() throws MBeanRuntimeException {

        long survivorPeakUsed;

        survivorPeakUsed = getPeakUsed(survivorSpacePoolObjectName);

        return survivorPeakUsed;

    }

    public long getTenuredPeakUsed() throws MBeanRuntimeException {

        long tenuredPeakUsed;

        tenuredPeakUsed = getPeakUsed(tenuredPoolObjectName);

        return tenuredPeakUsed;

    }

    private long getPeakUsed(ObjectName objectName) throws MBeanRuntimeException {

        long peakUsed = 0;

        try {
            MemoryUsage peakMemoryUsage = MemoryUsage.from((CompositeData) mbsc.getAttribute(objectName, "PeakUsage"));
            peakUsed = (long) peakMemoryUsage.getUsed();
        } catch (AttributeNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX AttributeNotFoundException: " + e.getMessage());
        } catch (InstanceNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX InstanceNotFoundException: " + e.getMessage());
        } catch (MBeanException e) {
            throw new MBeanRuntimeException(e, "JMX MBeanException: " + e.getMessage());
        } catch (ReflectionException e) {
            throw new MBeanRuntimeException(e, "JMX ReflectionException: " + e.getMessage());
        } catch (IOException e) {
            throw new MBeanRuntimeException(e, "JMX IOException: " + e.getMessage());
        }

        return peakUsed;
    }

    public void resetNurseryPeakUsage() throws MBeanRuntimeException {

        resetPeakUsage(nurseryPoolObjectName);

    }

    public void resetSurvivorPeakUsage() throws MBeanRuntimeException {

        resetPeakUsage(survivorSpacePoolObjectName);

    }

    public void resetTenuredPeakUsage() throws MBeanRuntimeException {

        resetPeakUsage(tenuredPoolObjectName);

    }

    private void resetPeakUsage(ObjectName objectName) throws MBeanRuntimeException {

        try {
            mbsc.invoke(objectName, "resetPeakUsage", null, null);
        } catch (InstanceNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX InstanceNotFoundException: " + e.getMessage());
        } catch (MBeanException e) {
            throw new MBeanRuntimeException(e, "JMX MBeanException: " + e.getMessage());
        } catch (ReflectionException e) {
            throw new MBeanRuntimeException(e, "JMX ReflectionException: " + e.getMessage());
        } catch (IOException e) {
            throw new MBeanRuntimeException(e, "JMX IOException: " + e.getMessage());
        }

    }

    public boolean isSingleGenerationHeap() {
        return this.onegen;
    }

}
