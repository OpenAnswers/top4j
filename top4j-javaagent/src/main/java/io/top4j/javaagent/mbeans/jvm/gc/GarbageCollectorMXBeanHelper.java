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

	public GarbageCollectorMXBeanHelper( MBeanServerConnection mbsc ) throws Exception {

		// store MBean server connection
		this.mbsc = mbsc;
        // get and store list of GarbageCollectorMXBean's
        this.gcbeans = ManagementFactory.getPlatformMXBeans( mbsc, GarbageCollectorMXBean.class );

		ObjectName nurseryCollectorObjectName = null;
		ObjectName tenuredCollectorObjectName = null;
		
		// discover nursery collector name
		String nurseryCollectorName = this.discoverNurseryCollectorName();
		this.setNurseryCollectorName(nurseryCollectorName);
		
		// discover tenured collector name
		String tenuredCollectorName = this.discoverTenuredCollectorName();
		this.setTenuredCollectorName(tenuredCollectorName);
		
		try {
			nurseryCollectorObjectName = new ObjectName("java.lang:type=GarbageCollector,name=" + nurseryCollectorName);
			tenuredCollectorObjectName = new ObjectName("java.lang:type=GarbageCollector,name=" + tenuredCollectorName);
		} catch (MalformedObjectNameException e) {
			throw new Exception( "JMX MalformedObjectNameException: " + e.getMessage() );
		}
		
		this.nurseryCollectorObjectName = nurseryCollectorObjectName;
		this.tenuredCollectorObjectName = tenuredCollectorObjectName;

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

	/** List all available garbage collector names */
	public List<String> listGarbageCollectorNames( ) {

		List<String> garbageCollectorNames = new ArrayList<>();

		for (GarbageCollectorMXBean gcMXBean : gcbeans) {

			String gcName = gcMXBean.getName();
			LOGGER.finest("Garbage Collector MX Bean Name = " + gcName);

			// add gcName to list of garbageCollectorNames
			garbageCollectorNames.add(gcName);
		}

		return garbageCollectorNames;

	}
	
	private String discoverNurseryCollectorName( ) throws Exception {
		
		String nurseryCollectorName = null;
		
		for (GarbageCollectorMXBean gcbean : gcbeans) {
			String name = gcbean.getName();
			LOGGER.finest("GC MX Bean Name = " + name);
			if (    name.equals("Copy") ||
                    name.equals("PS Scavenge") ||
                    name.endsWith("Young Collector") ||
                    name.equals("ParNew") ||
                    name.equals("G1 Young Generation")) {
				nurseryCollectorName = name;
			}
		}
		LOGGER.fine("Nursery Collector Name = " + nurseryCollectorName);
		if (nurseryCollectorName == null) {
			throw new Exception("Unable to auto discover nursery collector name.");
		}
		return nurseryCollectorName;
	}

	private String discoverTenuredCollectorName( ) throws Exception {
		
		String tenuredCollectorName = null;
		
		for (GarbageCollectorMXBean gcbean : gcbeans) {
			String name = gcbean.getName();
			LOGGER.finest("GC MX Bean Name = " + name);
			if (name.equals("MarkSweepCompact") ||
                    name.equals("PS MarkSweep") ||
                    name.endsWith("Old Collector") ||
                    name.equals("ConcurrentMarkSweep") ||
                    name.equals("G1 Old Generation")) {
				tenuredCollectorName = name;
			}
		}
		LOGGER.fine("Tenured Collector Name = " + tenuredCollectorName);
		if (tenuredCollectorName == null) {
			throw new Exception("Unable to auto discover tenured collector name.");
		}
		return tenuredCollectorName;
	}
	
	public long getGCTime( ) {
		
		long gcTime = 0;
		
		for (GarbageCollectorMXBean gcbean : gcbeans) {
			String name = gcbean.getName();
			long collectionTime = gcbean.getCollectionTime();
			LOGGER.finer(name + "Collector GC Collection Time = " + collectionTime);
			gcTime += collectionTime;
		}
		
		return gcTime;
		
	}
	
	public long getNurseryGCTime( ) throws Exception {
		
		long nurseryGCTime;
		
		nurseryGCTime = getCollectionTime( nurseryCollectorObjectName );
		
		return nurseryGCTime;
		
	}
	
	public long getTenuredGCTime( ) throws Exception {
		
		long tenuredGCTime;
		
		tenuredGCTime = getCollectionTime( tenuredCollectorObjectName );
		
		return tenuredGCTime;
		
	}
	
	private long getCollectionTime( ObjectName objectName ) throws Exception {
		
		long collectionTime = 0;
		
		try {
			collectionTime = (long) mbsc.getAttribute( objectName, "CollectionTime" );
		} catch (AttributeNotFoundException e) {
			throw new Exception( "JMX AttributeNotFoundException: " + e.getMessage() );
		} catch (InstanceNotFoundException e) {
			throw new Exception( "JMX InstanceNotFoundException: " + e.getMessage() );
		} catch (MBeanException e) {
			throw new Exception( "JMX MBeanException: " + e.getMessage() );
		} catch (ReflectionException e) {
			throw new Exception( "JMX ReflectionException: " + e.getMessage() );
		}
		
		return collectionTime;
	}
	
	public long getNurseryGCCount( ) throws Exception {
		
		long nurseryGCCount;
		
		nurseryGCCount = getCollectionCount( nurseryCollectorObjectName );
		
		return nurseryGCCount;
		
	}
	
	public long getTenuredGCCount( ) throws Exception {
		
		long tenuredGCCount = 0;
		
		tenuredGCCount = getCollectionCount( tenuredCollectorObjectName );
		
		return tenuredGCCount;
		
	}
	
	private long getCollectionCount( ObjectName objectName ) throws Exception {
		
		long collectionCount = 0;
		
		try {
			collectionCount = (long) mbsc.getAttribute( objectName, "CollectionCount" );
		} catch (AttributeNotFoundException e) {
			throw new Exception( "JMX AttributeNotFoundException: " + e.getMessage() );
		} catch (InstanceNotFoundException e) {
			throw new Exception( "JMX InstanceNotFoundException: " + e.getMessage() );
		} catch (MBeanException e) {
			throw new Exception( "JMX MBeanException: " + e.getMessage() );
		} catch (ReflectionException e) {
			throw new Exception( "JMX ReflectionException: " + e.getMessage() );
		}
		
		return collectionCount;
	}

}
