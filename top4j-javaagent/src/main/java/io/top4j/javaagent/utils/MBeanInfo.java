package io.top4j.javaagent.utils;

import java.util.Collection;

import javax.management.ObjectName;

public class MBeanInfo {
	
	private ObjectName objectName;
	private String keyPropertyList;
	private Collection<String> attributeNames;
	private String statsType;
	
	public ObjectName getObjectName() {
		return objectName;
	}
	public void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}
	public String getKeyPropertyList() {
		return keyPropertyList;
	}
	public void setKeyPropertyList(String keyPropertyList) {
		this.keyPropertyList = keyPropertyList;
	}
	public Collection<String> getAttributeNames() {
		return attributeNames;
	}
	public void setAttributeNames(Collection<String> attributeNames) {
		this.attributeNames = attributeNames;
	}
	public String getStatsType() {
		return statsType;
	}
	public void setStatsType(String statsType) {
		this.statsType = statsType;
	}
	
}
