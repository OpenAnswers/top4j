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

import io.top4j.javaagent.exception.MBeanDiscoveryException;
import io.top4j.javaagent.exception.MBeanInitException;
import io.top4j.javaagent.exception.MBeanRuntimeException;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.management.*;

public class GarbageCollectorMXBeanHelper {

    private String nurseryCollectorName;
    private String tenuredCollectorName;
    private ObjectName nurseryCollectorObjectName;
    private ObjectName tenuredCollectorObjectName;
    private List<GarbageCollectorMXBean> gcbeans;
    private MBeanServerConnection mbsc;

    private static final Logger LOGGER = Logger.getLogger(GarbageCollectorMXBeanHelper.class.getName());

    public GarbageCollectorMXBeanHelper(MBeanServerConnection mbsc) throws MBeanInitException {

        // store MBean server connection
        this.mbsc = mbsc;
        // get and store list of GarbageCollectorMXBean's
        try {
            this.gcbeans = ManagementFactory.getPlatformMXBeans(mbsc, GarbageCollectorMXBean.class);
        } catch (IOException e) {
            throw new MBeanInitException(e, "JMX IOException: " + e.getMessage());
        }

        try {
            // discover nursery collector name
            this.setNurseryCollectorName(this.discoverNurseryCollectorName());
            this.nurseryCollectorObjectName = new ObjectName("java.lang:type=GarbageCollector,name=" + nurseryCollectorName);

            if (!this.nurseryCollectorName.equals("ZGC")) {
                // discover tenured collector name
                this.setTenuredCollectorName(this.discoverTenuredCollectorName());
                this.tenuredCollectorObjectName = new ObjectName("java.lang:type=GarbageCollector,name=" + tenuredCollectorName);
            }
        } catch (MalformedObjectNameException e) {
            throw new MBeanInitException(e, "JMX MalformedObjectNameException: " + e.getMessage());
        }

    }

    public String getNurseryCollectorName() {
        return nurseryCollectorName;
    }

    public void setNurseryCollectorName(String nurseryCollectorName) {
        this.nurseryCollectorName = nurseryCollectorName;
    }

    public String getTenuredCollectorName() {
        return tenuredCollectorName;
    }

    public void setTenuredCollectorName(String tenuredCollectorName) {
        this.tenuredCollectorName = tenuredCollectorName;
    }

    /**
     * Lists all available garbage collector names
     * @return list of garbage collector names
     */
    public List<String> listGarbageCollectorNames() {

        List<String> garbageCollectorNames = new ArrayList<>();

        for (GarbageCollectorMXBean gcMXBean : gcbeans) {

            String gcName = gcMXBean.getName();
            LOGGER.finest("Garbage Collector MX Bean Name = " + gcName);

            // add gcName to list of garbageCollectorNames
            garbageCollectorNames.add(gcName);
        }

        return garbageCollectorNames;

    }

    private String discoverNurseryCollectorName() throws MBeanDiscoveryException {

        String collectorName = null;

        for (GarbageCollectorMXBean gcbean : gcbeans) {
            String name = gcbean.getName();
            LOGGER.finest("GC MX Bean Name = " + name);
            if (name.equals("Copy") ||
                    name.equals("PS Scavenge") ||
                    name.endsWith("Young Collector") ||
                    name.equals("ParNew") ||
                    name.equals("G1 Young Generation") ||
                    name.equals("ZGC")) {
                collectorName = name;
            }
        }
        LOGGER.fine("Nursery Collector Name = " + collectorName);
        if (collectorName == null) {
            throw new MBeanDiscoveryException("Unable to auto discover nursery collector name.");
        }
        return collectorName;
    }

    private String discoverTenuredCollectorName() throws MBeanDiscoveryException {

        String collectorName = null;

        for (GarbageCollectorMXBean gcbean : gcbeans) {
            String name = gcbean.getName();
            LOGGER.finest("GC MX Bean Name = " + name);
            if (name.equals("MarkSweepCompact") ||
                    name.equals("PS MarkSweep") ||
                    name.endsWith("Old Collector") ||
                    name.equals("ConcurrentMarkSweep") ||
                    name.equals("G1 Old Generation")) {
                collectorName = name;
            }
        }
        LOGGER.fine("Tenured Collector Name = " + collectorName);
        if (collectorName == null) {
            throw new MBeanDiscoveryException("Unable to auto discover tenured collector name.");
        }
        return collectorName;
    }

    public long getGCTime() {

        long gcTime = 0;

        for (GarbageCollectorMXBean gcbean : gcbeans) {
            String name = gcbean.getName();
            long collectionTime = gcbean.getCollectionTime();
            LOGGER.finer(name + "Collector GC Collection Time = " + collectionTime);
            gcTime += collectionTime;
        }

        return gcTime;

    }

    public long getNurseryGCTime() throws MBeanRuntimeException {

        long nurseryGCTime;

        nurseryGCTime = getCollectionTime(nurseryCollectorObjectName);

        return nurseryGCTime;

    }

    public long getTenuredGCTime() throws MBeanRuntimeException {

        long tenuredGCTime;

        tenuredGCTime = getCollectionTime(tenuredCollectorObjectName);

        return tenuredGCTime;

    }

    private long getCollectionTime(ObjectName objectName) throws MBeanRuntimeException {

        long collectionTime = 0;

        try {
            collectionTime = (long) mbsc.getAttribute(objectName, "CollectionTime");
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

        return collectionTime;
    }

    public long getNurseryGCCount() throws MBeanRuntimeException {

        long nurseryGCCount;

        nurseryGCCount = getCollectionCount(nurseryCollectorObjectName);

        return nurseryGCCount;

    }

    public long getTenuredGCCount() throws MBeanRuntimeException {

        long tenuredGCCount = 0;

        tenuredGCCount = getCollectionCount(tenuredCollectorObjectName);

        return tenuredGCCount;

    }

    private long getCollectionCount(ObjectName objectName) throws MBeanRuntimeException {

        long collectionCount = 0;

        try {
            collectionCount = (long) mbsc.getAttribute(objectName, "CollectionCount");
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

        return collectionCount;
    }

}
