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

package io.top4j.vm;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.cyclopsgroup.jmxterm.JavaProcess;
import org.cyclopsgroup.jmxterm.JavaProcessManager;

import java.io.IOException;
import java.util.*;

/**
 * JDK9 specific java process manager
 *
 */

public class Jdk9JavaProcessManager extends JavaProcessManager {

    private static final String LOCAL_CONNECTOR_ADDRESS_PROP =
            "com.sun.management.jmxremote.localConnectorAddress";

    @Override
    public JavaProcess get(int pid) {
        // retrieve list of attachable Java processes
        Map<Integer, JavaProcess> javaProcessMap = getAttachableVMs();
        // return Java process with pid process ID
        return javaProcessMap.get(pid);
    }

    @Override
    public List<JavaProcess> list() {
        // instantiate list of javaProcesses to return
        List<JavaProcess> javaProcesses = new ArrayList<>();
        // retrieve list of running Java processes via VirtualMachine API
        List<VirtualMachineDescriptor> virtualMachineDescriptors = VirtualMachine.list();
        String jvmid;
        String displayName;
        String address = null;
        int processId;
        for (VirtualMachineDescriptor virtualMachineDescriptor : virtualMachineDescriptors) {
            // get JVM ID
            jvmid = virtualMachineDescriptor.id();
            // get JVM display name
            displayName = virtualMachineDescriptor.displayName();
            try {
                // attach to JVM using VirtualMachine API
                VirtualMachine vm = VirtualMachine.attach(jvmid);
                // get JVM agent properties
                Properties agentProps = vm.getAgentProperties();
                // get JVM JMX local connector address
                address = (String) agentProps.get(LOCAL_CONNECTOR_ADDRESS_PROP);
                // detach from JVM
                vm.detach();
            } catch (AttachNotSupportedException e) {
                // not attachable
            } catch (IOException e) {
                // ignore
            }
            try {
                processId = Integer.parseInt(jvmid);
            }
            catch (NumberFormatException e) {
                // do not support JVMs where jvmid is different to PID
                continue;
            }
            // create new JavaProcess and add it to list of javaProcesses
            javaProcesses.add(new Jdk9JavaProcess(processId, displayName, address));
        }
        // return list of attachable Java processes
        return javaProcesses;
    }

    /**
     * Get Map containing list of attachable Java processes
     *
     * @return Map<Integer, JavaProcess> A Map containing a list of attachable Java processes
     */
    private Map<Integer, JavaProcess> getAttachableVMs() {
        // retrieve list of attachable Java processes
        List<JavaProcess> javaProcesses = list();
        // instantiate new Map to store list of attachable Java processes
        Map<Integer, JavaProcess> javaProcessMap = new HashMap<>();
        int processId;
        for (JavaProcess javaProcess : javaProcesses) {
            // get Java process ID (PID)
            processId = javaProcess.getProcessId();
            // add Java process to javaProcessMap
            javaProcessMap.put(processId, javaProcess);
        }
        return javaProcessMap;
    }
}
