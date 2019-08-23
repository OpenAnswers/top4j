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

import io.top4j.javaagent.mbeans.jvm.gc.GarbageCollectorMXBeanHelper;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Created by ryan on 02/06/15.
 */
public class GarbageCollectorNames {

    public static void main ( String[] args ) {

        // list garbage collector names
        list();
    }

    private static void list( ) {

        // get platform MBean server
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // instantiate new GarbageCollectorMXBeanHelper
        GarbageCollectorMXBeanHelper gcMxBeanHelper = null;
        try {
            gcMxBeanHelper = new GarbageCollectorMXBeanHelper( mbs );
        } catch (Exception e) {
            System.err.println("ERROR: Unable to initialise GarbageCollectorMXBeanHelper due to : " + e.getMessage() );
        }

        // get list of garbage collector names
        List<String> gcNames = gcMxBeanHelper.listGarbageCollectorNames();

        System.out.println("JVM Garbage Collector Names");
        System.out.println("===========================");
        for (String gcName : gcNames) {

            // print mpName to stdout
            System.out.println(gcName);
        }
    }
}
