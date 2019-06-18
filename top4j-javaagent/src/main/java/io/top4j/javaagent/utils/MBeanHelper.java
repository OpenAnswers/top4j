package io.top4j.javaagent.utils;

import io.top4j.javaagent.config.Constants;

import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

public class MBeanHelper {
	
	private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	private ObjectName objectName;
	
	public MBeanHelper ( String type, String statsType ) throws Exception {
		
		ObjectName objectName = null;
		
		try {
			objectName = new ObjectName(Constants.DOMAIN + ":type=" + type + ",statsType=" + statsType);
		} catch (MalformedObjectNameException e) {
			throw new Exception( "JMX MalformedObjectNameException: " + e.getMessage() );
		}

		this.objectName = objectName;

	}
	
	public MBeanHelper ( String type, String statsType, String rank ) throws Exception {
		
		ObjectName objectName = null;
		
		try {
			objectName = new ObjectName(Constants.DOMAIN + ":type=" + type + ",statsType=" + statsType + ",rank=" + rank);
		} catch (MalformedObjectNameException e) {
			throw new Exception( "JMX MalformedObjectNameException: " + e.getMessage() );
		}
		
		this.objectName = objectName;

	}
	
	public ObjectName getObjectName( ) {
		
		return this.objectName;
	
	}
	
	public void registerMBean ( Object obj ) throws Exception {

		try {
			mbs.registerMBean(obj, objectName);
		} catch (InstanceAlreadyExistsException e) {
			throw new Exception( "JMX InstanceAlreadyExistsException: " + e.getMessage() );
		} catch (MBeanRegistrationException e) {
			throw new Exception( "JMX MBeanRegistrationException: " + e.getMessage() );
		} catch (NotCompliantMBeanException e) {
			throw new Exception( "JMX NotCompliantMBeanException: " + e.getMessage() );
		}
		
	}
	
	public void updateMBeanAttribute ( String name, Object value ) throws Exception {
		
		// update objectName MBean attribute
    	Attribute mbeanAttribute = new Attribute(name, value );
    	try {
			mbs.setAttribute(objectName, mbeanAttribute);
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
	
	public void invokeMBeanOperation ( String operationName ) throws Exception {
		
		// invoke objectName MBean operation
		try {
			mbs.invoke(objectName, operationName, null, null);
		} catch (InstanceNotFoundException e) {
			throw new Exception( "JMX InstanceNotFoundException: " + e.getMessage() );
		} catch (ReflectionException e) {
			throw new Exception( "JMX ReflectionException: " + e.getMessage() );
		} catch (MBeanException e) {
			throw new Exception( "JMX MBeanException: " + e.getMessage() );
		}
	}
	
	public String invokeMBeanOperation ( String operationName, Object[] params, String[] signature ) throws Exception {
		
		String output = null;
		// invoke objectName MBean operation
		try {
			output = (String) mbs.invoke(objectName, operationName, params, signature);
		} catch (InstanceNotFoundException e) {
			throw new Exception( "JMX InstanceNotFoundException: " + e.getMessage() );
		} catch (ReflectionException e) {
			throw new Exception( "JMX ReflectionException: " + e.getMessage() );
		} catch (MBeanException e) {
			throw new Exception( "JMX MBeanException: " + e.getMessage() );
		}
		
		return output;
	}

}
