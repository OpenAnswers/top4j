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
