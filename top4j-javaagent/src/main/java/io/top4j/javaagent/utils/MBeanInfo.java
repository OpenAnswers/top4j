/*
 * Copyright (c) 2019 Open Answers Ltd.
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
