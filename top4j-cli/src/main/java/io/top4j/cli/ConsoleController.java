package io.top4j.cli;

import io.top4j.javaagent.config.Constants;
import io.top4j.javaagent.mbeans.jvm.heap.HeapStatsMXBean;
import io.top4j.javaagent.mbeans.jvm.memory.MemoryStatsMXBean;
import io.top4j.javaagent.mbeans.jvm.threads.ThreadStatsMXBean;
import io.top4j.javaagent.mbeans.jvm.threads.TopThreadMXBean;
import io.top4j.javaagent.utils.ThreadHelper;
import io.top4j.javaagent.mbeans.jvm.gc.GCStatsMXBean;
import jline.console.ConsoleReader;

import javax.management.*;
import java.io.IOException;
import java.lang.management.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ryan on 02/02/16.
 */
public class ConsoleController  extends TimerTask {

    private final ConsoleReader consoleReader;
    private final Display display;
    private final MBeanServerConnection mbsc;
    private final MBeanServer localMBS = ManagementFactory.getPlatformMBeanServer();
    private final GCStatsMXBean gcStatsMXBean;
    private final MemoryStatsMXBean memoryStatsMXBean;
    private final HeapStatsMXBean heapStatsMXBean;
    private final ThreadStatsMXBean threadStatsMXBean;
    private List<TopThreadMXBean> topThreadMXBeans = new ArrayList<>();
    private final ThreadMXBean threadMXBean;
    private final RuntimeMXBean runtimeMXBean;
    private final OperatingSystemMXBean osMXBean;
    private Map<Integer, Long> threadIds = new HashMap<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private ThreadHelper threadHelper;
    private final int MAX_THREAD_NAME_LENGTH = 49;

    public ConsoleController ( ConsoleReader consoleReader, Display display, MBeanServerConnection mbsc, int topThreadCount ) {

        this.consoleReader = consoleReader;
        this.display = display;
        this.mbsc = mbsc;
        try {
            this.threadHelper = new ThreadHelper( mbsc );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create GCStats objectName
        ObjectName gcStatsObjectName = null;
        try {
            gcStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.GC_STATS_TYPE );
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        // instantiate new gcStatsMXBean proxy based on gcStatsObjectName
        this.gcStatsMXBean = JMX.newMBeanProxy(localMBS, gcStatsObjectName, GCStatsMXBean.class);

        // create MemoryStats objectName
        ObjectName memoryStatsObjectName = null;
        try {
            memoryStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.MEMORY_STATS_TYPE );
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        // instantiate new memoryStatsMXBean proxy based on memoryStatsObjectName
        this.memoryStatsMXBean = JMX.newMBeanProxy(localMBS, memoryStatsObjectName, MemoryStatsMXBean.class);

        // create HeapStats objectName
        ObjectName heapStatsObjectName = null;
        try {
            heapStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.HEAP_STATS_TYPE );
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        // instantiate new heapStatsMXBean proxy based on heapStatsObjectName
        this.heapStatsMXBean = JMX.newMBeanProxy(localMBS, heapStatsObjectName, HeapStatsMXBean.class);

        // create ThreadStats objectName
        ObjectName threadStatsObjectName = null;
        try {
            threadStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.THREADS_STATS_TYPE );
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        // instantiate new threadStatsMXBean proxy based on threadStatsObjectName
        this.threadStatsMXBean = JMX.newMBeanProxy(localMBS, threadStatsObjectName, ThreadStatsMXBean.class);

        // populate topThread MBean list
        for (int rank =1; rank <=topThreadCount; rank++) {

            // create TopThread objectName
            ObjectName topThreadObjectName = null;
            try {
                topThreadObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.TOP_THREAD_STATS_TYPE + ",rank=" + rank);
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }
            // instantiate and store topThreadMXBean proxy based on topThreadObjectName
            this.topThreadMXBeans.add(JMX.newMBeanProxy(localMBS, topThreadObjectName, TopThreadMXBean.class));
        }

        // create ThreadMXBean objectName
        ObjectName threadMXBeanObjectName = null;
        try {
            threadMXBeanObjectName = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        // instantiate new threadMXBean proxy based on threadMXBeanObjectName
        this.threadMXBean = JMX.newMBeanProxy(mbsc, threadMXBeanObjectName, ThreadMXBean.class);

        // create RuntimeMXBean objectName
        ObjectName runtimeMXBeanObjectName = null;
        try {
            runtimeMXBeanObjectName = new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        // instantiate new runtimeMXBean proxy based on runtimeMXBeanObjectName
        this.runtimeMXBean = JMX.newMBeanProxy(mbsc, runtimeMXBeanObjectName, RuntimeMXBean.class);

        // create RuntimeMXBean objectName
        ObjectName osMXBeanObjectName = null;
        try {
            osMXBeanObjectName = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        // instantiate new osMXBean proxy based on osMXBeanObjectName
        this.osMXBean = JMX.newMBeanProxy(mbsc, osMXBeanObjectName, OperatingSystemMXBean.class);

    }

    @Override
    public void run() {

        Character userInput = (char) Integer.valueOf( display.getText() ).intValue();
        String screen;
        if ( Character.isDigit(userInput) ) {
            screen = createThreadStackTraceScreen(Character.getNumericValue(userInput));
        }
        else if ( userInput.toString().equals("q")) {
            screen = null;
            try {
                consoleReader.println("Exiting....");
                consoleReader.println();
                consoleReader.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // exit Top4J
            System.exit(0);
        }
        else {
            screen = createTopThreadsScreen();
        }
        try {
            consoleReader.clearScreen();
            consoleReader.println(screen);
            consoleReader.println();
            //consoleReader.println("Terminal Width: " + new Integer(consoleReader.getTerminal().getWidth()).toString());
            //consoleReader.println("Terminal Height: " + new Integer(consoleReader.getTerminal().getHeight()).toString());
            //consoleReader.println();
            //consoleReader.println("Test text: " + display.getText());
            //consoleReader.println();
            consoleReader.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createTopThreadsScreen() {

        Date date = new Date();
        StringBuffer sb = new StringBuffer();
        sb.append("top4j - " + timeFormat.format(date) + " up " + ( runtimeMXBean.getUptime() / 1000 )  + " secs,  load average: " + osMXBean.getSystemLoadAverage() + "\n");
        sb.append("Threads: " + threadStatsMXBean.getThreadCount() + " total,   " +
                threadStatsMXBean.getRunnableThreadCount() + " runnable,   " +
                threadStatsMXBean.getWaitingThreadCount() + " waiting,   " +
                threadStatsMXBean.getTimedWaitingThreadCount() + " timed waiting,   " +
                threadStatsMXBean.getBlockedThreadCount() + " blocked\n");
        sb.append("%Cpu(s): " + String.format("%.2f", threadStatsMXBean.getCpuUsage()) + " total,  " +
                String.format("%.2f", threadStatsMXBean.getUserCpuUsage()) + " user,  " +
                String.format("%.2f", threadStatsMXBean.getSysCpuUsage()) + " sys\n");
        sb.append("Heap Util(%):        " + String.format( "%.2f", heapStatsMXBean.getEdenSpaceUtil() ) + " eden,        " +
                String.format( "%.2f", heapStatsMXBean.getSurvivorSpaceUtil() ) + " survivor,        " +
                String.format( "%.2f", heapStatsMXBean.getTenuredHeapUtil() ) + " tenured\n");
        sb.append("Mem Alloc(MB/s):     " + String.format( "%.2f", memoryStatsMXBean.getMemoryAllocationRate() ) + " eden,        " +
                String.format( "%.2f", memoryStatsMXBean.getMemorySurvivorRate() ) + " survivor,        " +
                String.format("%.2f", memoryStatsMXBean.getMemoryPromotionRate()) + " tenured\n");
        sb.append("GC Stats:  " + String.format("%.4f", gcStatsMXBean.getGcOverhead()) + "% GC overhead\n");
        sb.append("\n");
        sb.append("#  TID     THREAD NAME                                       %CPU\n");
        // initialise thread counter
        int counter = 0;
        for (TopThreadMXBean topThreadMXBean : topThreadMXBeans) {

            String threadName = topThreadMXBean.getThreadName();
            if (threadName != null && threadName.length() > MAX_THREAD_NAME_LENGTH) {
               threadName = threadName.substring(0, MAX_THREAD_NAME_LENGTH-1);
            }
            sb.append(  counter + "  " +
                    String.format("%1$-8s", topThreadMXBean.getThreadId()) +
                    String.format("%1$-50s", threadName) +
                    String.format( "%.1f", topThreadMXBean.getThreadCpuUsage() ) +
                    "\n");

            // store thread Id
            threadIds.put(counter, topThreadMXBean.getThreadId() );
            // increment thread counter
            counter++;
        }
        sb.append("\n\n");
        sb.append("Hit [0-9] to view thread stack trace, [q] to quit\n");

        return sb.toString();

    }

    private String createThreadStackTraceScreen( int threadNumber ) {

        Date date = new Date();
        StringBuffer sb = new StringBuffer();
        long threadId = threadIds.get(threadNumber);
        TopThreadMXBean topThreadMXBean = topThreadMXBeans.get(threadNumber);
        sb.append("top4j - " + timeFormat.format(date) + " up " + ( runtimeMXBean.getUptime() / 1000 )  + " secs,  load average: " + osMXBean.getSystemLoadAverage() + "\n");
        sb.append("\n");
        sb.append(threadHelper.getStackTraceWithContext(threadId, 15));
        sb.append("\n\n");
        sb.append("Hit [m] to return to main screen, [q] to quit\n");

        return sb.toString();
    }

    private String getUpTime( ) {
        return "";
    }
}
