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

package io.top4j.javaagent.utils;

import io.top4j.javaagent.config.Constants;
import io.top4j.javaagent.exception.MBeanInitException;
import io.top4j.javaagent.exception.MBeanRuntimeException;

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

    public MBeanHelper(String type, String statsType) throws MBeanInitException {

        ObjectName objectName = null;

        try {
            objectName = new ObjectName(Constants.DOMAIN + ":type=" + type + ",statsType=" + statsType);
        } catch (MalformedObjectNameException e) {
            throw new MBeanInitException(e, "JMX MalformedObjectNameException: " + e.getMessage());
        }

        this.objectName = objectName;

    }

    public MBeanHelper(String type, String statsType, String rank) throws MBeanInitException {

        ObjectName objectName = null;

        try {
            objectName = new ObjectName(Constants.DOMAIN + ":type=" + type + ",statsType=" + statsType + ",rank=" + rank);
        } catch (MalformedObjectNameException e) {
            throw new MBeanInitException(e, "JMX MalformedObjectNameException: " + e.getMessage());
        }

        this.objectName = objectName;

    }

    public ObjectName getObjectName() {

        return this.objectName;

    }

    public void registerMBean(Object obj) throws MBeanInitException {

        try {
            mbs.registerMBean(obj, objectName);
        } catch (InstanceAlreadyExistsException e) {
            throw new MBeanInitException(e, "JMX InstanceAlreadyExistsException: " + e.getMessage());
        } catch (MBeanRegistrationException e) {
            throw new MBeanInitException(e, "JMX MBeanRegistrationException: " + e.getMessage());
        } catch (NotCompliantMBeanException e) {
            throw new MBeanInitException(e, "JMX NotCompliantMBeanException: " + e.getMessage());
        }

    }

    public void updateMBeanAttribute(String name, Object value) throws MBeanRuntimeException {

        // update objectName MBean attribute
        Attribute mbeanAttribute = new Attribute(name, value);
        try {
            mbs.setAttribute(objectName, mbeanAttribute);
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
        }

    }

    public void invokeMBeanOperation(String operationName) throws MBeanRuntimeException {

        // invoke objectName MBean operation
        try {
            mbs.invoke(objectName, operationName, null, null);
        } catch (InstanceNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX InstanceNotFoundException: " + e.getMessage());
        } catch (ReflectionException e) {
            throw new MBeanRuntimeException(e, "JMX ReflectionException: " + e.getMessage());
        } catch (MBeanException e) {
            throw new MBeanRuntimeException(e, "JMX MBeanException: " + e.getMessage());
        }
    }

    public String invokeMBeanOperation(String operationName, Object[] params, String[] signature) throws MBeanRuntimeException {

        String output = null;
        // invoke objectName MBean operation
        try {
            output = (String) mbs.invoke(objectName, operationName, params, signature);
        } catch (InstanceNotFoundException e) {
            throw new MBeanRuntimeException(e, "JMX InstanceNotFoundException: " + e.getMessage());
        } catch (ReflectionException e) {
            throw new MBeanRuntimeException(e, "JMX ReflectionException: " + e.getMessage());
        } catch (MBeanException e) {
            throw new MBeanRuntimeException(e, "JMX MBeanException: " + e.getMessage());
        }

        return output;
    }

}
