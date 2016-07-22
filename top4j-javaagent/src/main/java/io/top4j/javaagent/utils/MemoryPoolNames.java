package io.top4j.javaagent.utils;

import io.top4j.javaagent.mbeans.jvm.memory.MemoryPoolMXBeanHelper;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Created by ryan on 02/06/15.
 */
public class MemoryPoolNames {

    public static void main ( String[] args ) {

        // list memory pool names
        list();
    }

    private static void list( ) {

        // get platform MBean server
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // instantiate new MemoryPoolMXBeanHelper
        MemoryPoolMXBeanHelper memoryPoolMxBeanHelper = null;
        try {
            memoryPoolMxBeanHelper = new MemoryPoolMXBeanHelper( mbs );
        } catch (Exception e) {
            System.err.println("ERROR: Unable to initialise MemoryPoolMXBeanHelper due to : " + e.getMessage() );
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
