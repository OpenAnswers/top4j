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
import org.cyclopsgroup.jmxterm.JavaProcess;

import java.io.IOException;
import java.util.Properties;

/**
 * JDK9 specific java process
 *
 */

public class Jdk9JavaProcess implements JavaProcess {

    private final int id;
    private final String name;
    private String address;
    private static final String LOCAL_CONNECTOR_ADDRESS_PROP =
            "com.sun.management.jmxremote.localConnectorAddress";

    public Jdk9JavaProcess(int jvmid, String displayName, String address ) {
        this.id = jvmid;
        this.name = displayName;
        this.address = address;
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }

    @Override
    public int getProcessId() {
        return this.id;
    }

    @Override
    public boolean isManageable() {
        return (address != null);
    }

    @Override
    public void startManagementAgent() throws IOException {

        if (address != null) {
            // management agent already started
            return;
        }

        // load management agent
        loadManagementAgent();

        // failed to load or start the management agent
        if (address == null) {
            throw new IOException("Failed to start the JVM management agent");
        }
    }

    @Override
    public String toUrl() {
        return address;
    }

    // load the management agent into the target VM
    private void loadManagementAgent() throws IOException {
        VirtualMachine vm;
        String name = String.valueOf(id);
        try {
            // attach to JVM using VirtualMachine API
            vm = VirtualMachine.attach(name);
        } catch (AttachNotSupportedException e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }

        // start the JVM JMX management agent
        vm.startLocalManagementAgent();

        // get the connector address
        Properties agentProps = vm.getAgentProperties();
        address = (String) agentProps.get(LOCAL_CONNECTOR_ADDRESS_PROP);

        vm.detach();
    }
}
