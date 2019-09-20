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

import io.top4j.javaagent.mbeans.jvm.memory.MemoryPoolMXBeanHelper;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.List;

public class MemoryPoolNames {

    public static void main(String[] args) {

        // list memory pool names
        list();
    }

    private static void list() {

        // get platform MBean server
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // instantiate new MemoryPoolMXBeanHelper
        MemoryPoolMXBeanHelper memoryPoolMxBeanHelper = null;
        try {
            memoryPoolMxBeanHelper = new MemoryPoolMXBeanHelper(mbs);
        } catch (Exception e) {
            System.err.println("ERROR: Unable to initialise MemoryPoolMXBeanHelper due to : " + e.getMessage());
        }

        // get list of memory pool names
        List<String> memoryPoolNames = memoryPoolMxBeanHelper.listMemoryPoolNames();

        System.out.println("JVM Memory Pool Names");
        System.out.println("=====================");
        for (String mpName : memoryPoolNames) {

            // print mpName to stdout
            System.out.println(mpName);
        }
    }
}
