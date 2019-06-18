package io.top4j.javaagent.mbeans.jvm.memory;

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

	private static final Logger LOGGER = Logger.getLogger(MemoryPoolMXBeanHelper.class.getName());
	
	public MemoryPoolMXBeanHelper( MBeanServerConnection mbsc ) throws Exception {

		// store MBean server connection
		this.mbsc = mbsc;
        // get and store list of MemoryPoolMXBean
        this.memPoolMXBeans = ManagementFactory.getPlatformMXBeans( mbsc, MemoryPoolMXBean.class );

		ObjectName nurseryPoolObjectName = null;
		ObjectName survivorSpacePoolObjectName = null;
		ObjectName tenuredPoolObjectName = null;
		
		// discover nursery Pool name
		String nurseryPoolName = this.discoverNurseryPoolName();
		this.setNurseryPoolName(nurseryPoolName);
		
		// discover survivor space Pool name
		String survivorSpacePoolName = this.discoverSurvivorSpacePoolName();
		this.setSurvivorSpacePoolName(survivorSpacePoolName);
		
		// discover tenured Pool name
		String tenuredPoolName = this.discoverTenuredPoolName();
		this.setTenuredPoolName(tenuredPoolName);
		
		try {
			nurseryPoolObjectName = new ObjectName("java.lang:type=MemoryPool,name=" + nurseryPoolName);
			survivorSpacePoolObjectName = new ObjectName("java.lang:type=MemoryPool,name=" + survivorSpacePoolName);
			tenuredPoolObjectName = new ObjectName("java.lang:type=MemoryPool,name=" + tenuredPoolName);
		} catch (MalformedObjectNameException e) {
			throw new Exception( "JMX MalformedObjectNameException: " + e.getMessage() );
		}
		
		this.nurseryPoolObjectName = nurseryPoolObjectName;
		this.survivorSpacePoolObjectName = survivorSpacePoolObjectName;
		this.tenuredPoolObjectName = tenuredPoolObjectName;

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

	/** List all available memory pool names */
	public List<String> listMemoryPoolNames( ) {

		List<String> memoryPoolNames = new ArrayList<>();

		for (MemoryPoolMXBean memPoolMXBean : memPoolMXBeans) {

			String mpName = memPoolMXBean.getName();
			LOGGER.finest("Memory Pool MX Bean Name = " + mpName);

			// add mpName to list of memoryPoolNames
			memoryPoolNames.add(mpName);
		}

		return memoryPoolNames;

	}
	
	private String discoverNurseryPoolName( ) throws Exception {
		
		String nurseryPoolName = null;
				
		for (MemoryPoolMXBean memPoolMXBean : memPoolMXBeans) {
			
			String mpName = memPoolMXBean.getName();
			LOGGER.finest("Memory Pool MX Bean Name = " + mpName);
			
    		if (    mpName.endsWith("Eden Space") ||
					mpName.equals("Nursery")) {
    			nurseryPoolName = mpName;
    		}
			
		}
		LOGGER.fine("Nursery Pool Name = " + nurseryPoolName);
		if (nurseryPoolName == null) {
			throw new Exception("Unable to auto discover nursery pool name.");
		}
		return nurseryPoolName;
		
	}
	
	private String discoverSurvivorSpacePoolName( ) throws Exception {
		
		String survivorSpacePoolName = null;
				
		for (MemoryPoolMXBean memPoolMXBean : memPoolMXBeans) {
			
			String mpName = memPoolMXBean.getName();
			LOGGER.finest("Memory Pool MX Bean Name = " + mpName);
			
    		if (mpName.endsWith("Survivor Space")) {
    			survivorSpacePoolName = mpName;
    		}
			
		}
		LOGGER.fine("Survivor Space Pool Name = " + survivorSpacePoolName);
		if (nurseryPoolName == null) {
			throw new Exception("Unable to auto discover survivor space pool name.");
		}
		return survivorSpacePoolName;
		
	}
	
	private String discoverTenuredPoolName( ) throws Exception {
		
		String tenuredPoolName = null;
				
		for (MemoryPoolMXBean memPoolMXBean : memPoolMXBeans) {
			
			String mpName = memPoolMXBean.getName();
			LOGGER.finest("Memory Pool MX Bean Name = " + mpName);
			
			if (    mpName.equals("Tenured Gen") ||
					mpName.endsWith("Old Gen") ||
					mpName.equals("Old Space")) {
    			tenuredPoolName = mpName;
    		}
			
		}
		LOGGER.fine("Tenured Pool Name = " + tenuredPoolName);
		if (nurseryPoolName == null) {
			throw new Exception("Unable to auto discover tenured pool name.");
		}
		return tenuredPoolName;
		
	}
	
	public long getNurseryHeapUsed( ) throws Exception {
		
		long nurseryHeapUsed;
		
		nurseryHeapUsed = getHeapUsed( nurseryPoolObjectName );
		
		return nurseryHeapUsed;
		
	}
	
	public long getSurvivorSpaceUsed( ) throws Exception {
		
		long survivorSpaceUsed;
		
		survivorSpaceUsed = getHeapUsed( survivorSpacePoolObjectName );
		
		return survivorSpaceUsed;
		
	}
	
	public long getTenuredHeapUsed( ) throws Exception {
		
		long tenuredHeapUsed;
		
		tenuredHeapUsed = getHeapUsed( tenuredPoolObjectName );
		
		return tenuredHeapUsed;
		
	}
	
	private long getHeapUsed( ObjectName objectName ) throws Exception {
		
		long heapUsed = 0;
		
		try {
			MemoryUsage memoryUsage = MemoryUsage.from( (CompositeData) mbsc.getAttribute( objectName, "Usage" ) );
			heapUsed = (long) memoryUsage.getUsed();
		} catch (AttributeNotFoundException e) {
			throw new Exception( "JMX AttributeNotFoundException: " + e.getMessage() );
		} catch (InstanceNotFoundException e) {
			throw new Exception( "JMX InstanceNotFoundException: " + e.getMessage() );
		} catch (MBeanException e) {
			throw new Exception( "JMX MBeanException: " + e.getMessage() );
		} catch (ReflectionException e) {
			throw new Exception( "JMX ReflectionException: " + e.getMessage() );
		}
		
		return heapUsed;
	}
	
	public long getNurseryHeapCommitted( ) throws Exception {
		
		long nurseryHeapCommitted;
		
		nurseryHeapCommitted = getHeapCommitted( nurseryPoolObjectName );
		
		return nurseryHeapCommitted;
		
	}
	
	public long getSurvivorSpaceCommitted( ) throws Exception {
		
		long survivorSpaceCommitted;
		
		survivorSpaceCommitted = getHeapCommitted( survivorSpacePoolObjectName );
		
		return survivorSpaceCommitted;
		
	}
	
	public long getTenuredHeapCommitted( ) throws Exception {
		
		long tenuredHeapCommitted;
		
		tenuredHeapCommitted = getHeapCommitted( tenuredPoolObjectName );
		
		return tenuredHeapCommitted;
		
	}
	
	private long getHeapCommitted( ObjectName objectName ) throws Exception {
		
		long heapCommitted = 0;
		
		try {
			MemoryUsage memoryUsage = MemoryUsage.from( (CompositeData) mbsc.getAttribute( objectName, "Usage" ) );
			heapCommitted = (long) memoryUsage.getCommitted();
		} catch (AttributeNotFoundException e) {
			throw new Exception( "JMX AttributeNotFoundException: " + e.getMessage() );
		} catch (InstanceNotFoundException e) {
			throw new Exception( "JMX InstanceNotFoundException: " + e.getMessage() );
		} catch (MBeanException e) {
			throw new Exception( "JMX MBeanException: " + e.getMessage() );
		} catch (ReflectionException e) {
			throw new Exception( "JMX ReflectionException: " + e.getMessage() );
		}
		
		return heapCommitted;
	}
	
	public long getNurseryCollectionUsed( ) throws Exception {
		
		long nurseryCollectionUsed;
		
		nurseryCollectionUsed = getCollectionUsed( nurseryPoolObjectName );
		
		return nurseryCollectionUsed;
		
	}
	
	public long getSurvivorCollectionUsed( ) throws Exception {
		
		long survivorCollectionUsed;
		
		survivorCollectionUsed = getCollectionUsed( survivorSpacePoolObjectName );
		
		return survivorCollectionUsed;
		
	}
	
	public long getTenuredCollectionUsed( ) throws Exception {
		
		long tenuredCollectionUsed;
		
		tenuredCollectionUsed = getCollectionUsed( tenuredPoolObjectName );
		
		return tenuredCollectionUsed;
		
	}
	
	private long getCollectionUsed( ObjectName objectName ) throws Exception {
		
		long collectionUsed = 0;
		
		try {
			MemoryUsage memoryUsage = MemoryUsage.from( (CompositeData) mbsc.getAttribute( objectName, "CollectionUsage" ) );
			collectionUsed = (long) memoryUsage.getUsed();
		} catch (AttributeNotFoundException e) {
			throw new Exception( "JMX AttributeNotFoundException: " + e.getMessage() );
		} catch (InstanceNotFoundException e) {
			throw new Exception( "JMX InstanceNotFoundException: " + e.getMessage() );
		} catch (MBeanException e) {
			throw new Exception( "JMX MBeanException: " + e.getMessage() );
		} catch (ReflectionException e) {
			throw new Exception( "JMX ReflectionException: " + e.getMessage() );
		}
		
		return collectionUsed;
	}
	
	public long getNurseryCollectionCommitted( ) throws Exception {
		
		long nurseryCollectionCommitted;
		
		nurseryCollectionCommitted = getCollectionCommitted( nurseryPoolObjectName );
		
		return nurseryCollectionCommitted;
		
	}
	
	public long getSurvivorCollectionCommitted( ) throws Exception {
		
		long survivorCollectionCommitted;
		
		survivorCollectionCommitted = getCollectionCommitted( survivorSpacePoolObjectName );
		
		return survivorCollectionCommitted;
		
	}
	
	public long getTenuredCollectionCommitted( ) throws Exception {
		
		long tenuredCollectionCommitted;
		
		tenuredCollectionCommitted = getCollectionCommitted( tenuredPoolObjectName );
		
		return tenuredCollectionCommitted;
		
	}
	
	private long getCollectionCommitted( ObjectName objectName ) throws Exception {
		
		long collectionCommitted = 0;
		
		try {
			MemoryUsage collectionUsage = MemoryUsage.from( (CompositeData) mbsc.getAttribute( objectName, "CollectionUsage" ) );
			collectionCommitted = (long) collectionUsage.getCommitted();
		} catch (AttributeNotFoundException e) {
			throw new Exception( "JMX AttributeNotFoundException: " + e.getMessage() );
		} catch (InstanceNotFoundException e) {
			throw new Exception( "JMX InstanceNotFoundException: " + e.getMessage() );
		} catch (MBeanException e) {
			throw new Exception( "JMX MBeanException: " + e.getMessage() );
		} catch (ReflectionException e) {
			throw new Exception( "JMX ReflectionException: " + e.getMessage() );
		}
		
		return collectionCommitted;
	}

	public void setNurseryCollectionUsageThreshold( long threshold ) throws Exception {
		
		setCollectionUsageThreshold(nurseryPoolObjectName, threshold);
		
	}
	
	public void setSurvivorCollectionUsageThreshold( long threshold ) throws Exception {
		
		setCollectionUsageThreshold(survivorSpacePoolObjectName, threshold);
		
	}
	
	public void setTenuredCollectionUsageThreshold( long threshold ) throws Exception {
		
		setCollectionUsageThreshold(tenuredPoolObjectName, threshold);
		
	}
	
	private void setCollectionUsageThreshold( ObjectName objectName, long threshold ) throws Exception {
	
		Attribute collectionUsageThreshold = new Attribute("CollectionUsageThreshold", threshold);
		try {
			mbsc.setAttribute(objectName, collectionUsageThreshold);
		} catch (InstanceNotFoundException e) {
			throw new Exception( "JMX InstanceNotFoundException: " + e.getMessage() );
		} catch (InvalidAttributeValueException e) {
			throw new Exception( "JMX InvalidAttributeValueException: " + e.getMessage() );
		} catch (AttributeNotFoundException e) {
			throw new Exception( "JMX AttributeNotFoundException: " + e.getMessage() );
		} catch (ReflectionException e) {
			throw new Exception( "JMX ReflectionException: " + e.getMessage() );
		} catch (MBeanException e) {
			throw new Exception( "JMX MBeanException: " + e.getMessage() );
		}
		
	}
	
	public void setNurseryUsageThreshold( long threshold ) throws Exception {
		
		setUsageThreshold(nurseryPoolObjectName, threshold);
		
	}
	
	public void setSurvivorUsageThreshold( long threshold ) throws Exception {
		
		setUsageThreshold(survivorSpacePoolObjectName, threshold);
		
	}
	
	public void setTenuredUsageThreshold( long threshold ) throws Exception {
		
		setUsageThreshold(tenuredPoolObjectName, threshold);
		
	}
	
	private void setUsageThreshold( ObjectName objectName, long threshold ) throws Exception {
	
		Attribute usageThreshold = new Attribute("UsageThreshold", threshold);
		try {
			mbsc.setAttribute(objectName, usageThreshold);
		} catch (InstanceNotFoundException e) {
			throw new Exception( "JMX InstanceNotFoundException: " + e.getMessage() );
		} catch (InvalidAttributeValueException e) {
			throw new Exception( "JMX InvalidAttributeValueException: " + e.getMessage() );
		} catch (AttributeNotFoundException e) {
			throw new Exception( "JMX AttributeNotFoundException: " + e.getMessage() );
		} catch (ReflectionException e) {
			throw new Exception( "JMX ReflectionException: " + e.getMessage() );
		} catch (MBeanException e) {
			throw new Exception( "JMX MBeanException: " + e.getMessage() );
		}
		
	}
	
	public long getNurseryPeakUsed( ) throws Exception {
		
		long nurseryPeakUsed;
		
		nurseryPeakUsed = getPeakUsed( nurseryPoolObjectName );
		
		return nurseryPeakUsed;
		
	}
	
	public long getSurvivorPeakUsed( ) throws Exception {
		
		long survivorPeakUsed;
		
		survivorPeakUsed = getPeakUsed( survivorSpacePoolObjectName );
		
		return survivorPeakUsed;
		
	}
	
	public long getTenuredPeakUsed( ) throws Exception {
		
		long tenuredPeakUsed;
		
		tenuredPeakUsed = getPeakUsed( tenuredPoolObjectName );
		
		return tenuredPeakUsed;
		
	}
	
	private long getPeakUsed( ObjectName objectName ) throws Exception {
		
		long peakUsed = 0;
		
		try {
			MemoryUsage peakMemoryUsage = MemoryUsage.from( (CompositeData) mbsc.getAttribute( objectName, "PeakUsage" ) );
			peakUsed = (long) peakMemoryUsage.getUsed();
		} catch (AttributeNotFoundException e) {
			throw new Exception( "JMX AttributeNotFoundException: " + e.getMessage() );
		} catch (InstanceNotFoundException e) {
			throw new Exception( "JMX InstanceNotFoundException: " + e.getMessage() );
		} catch (MBeanException e) {
			throw new Exception( "JMX MBeanException: " + e.getMessage() );
		} catch (ReflectionException e) {
			throw new Exception( "JMX ReflectionException: " + e.getMessage() );
		}
		
		return peakUsed;
	}
	
	public void resetNurseryPeakUsage( ) throws Exception {
		
		resetPeakUsage( nurseryPoolObjectName );
		
	}
	
	public void resetSurvivorPeakUsage( ) throws Exception {
		
		resetPeakUsage( survivorSpacePoolObjectName );
		
	}
	
	public void resetTenuredPeakUsage( ) throws Exception {
		
		resetPeakUsage( tenuredPoolObjectName );
		
	}
	
	private void resetPeakUsage( ObjectName objectName ) throws Exception {
		
		try {
			mbsc.invoke(objectName, "resetPeakUsage", null, null);
		} catch (InstanceNotFoundException e) {
			throw new Exception( "JMX InstanceNotFoundException: " + e.getMessage() );
		} catch (MBeanException e) {
			throw new Exception( "JMX MBeanException: " + e.getMessage() );
		} catch (ReflectionException e) {
			throw new Exception( "JMX ReflectionException: " + e.getMessage() );
		}
		
	}

}
