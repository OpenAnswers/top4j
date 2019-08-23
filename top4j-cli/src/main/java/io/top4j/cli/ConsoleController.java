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

package io.top4j.cli;

import io.top4j.javaagent.config.Constants;
import io.top4j.javaagent.mbeans.jvm.heap.HeapStatsMXBean;
import io.top4j.javaagent.mbeans.jvm.memory.MemoryStatsMXBean;
import io.top4j.javaagent.mbeans.jvm.threads.BlockedThreadMXBean;
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
    private final UserInput userInput;
    private final MBeanServerConnection mbsc;
    private final MBeanServer localMBS = ManagementFactory.getPlatformMBeanServer();
    private final GCStatsMXBean gcStatsMXBean;
    private final MemoryStatsMXBean memoryStatsMXBean;
    private final HeapStatsMXBean heapStatsMXBean;
    private final ThreadStatsMXBean threadStatsMXBean;
    private List<TopThreadMXBean> topThreadMXBeans = new ArrayList<>();
    private List<BlockedThreadMXBean> blockedThreadMXBeans = new ArrayList<>();
    private final ThreadMXBean threadMXBean;
    private final RuntimeMXBean runtimeMXBean;
    private final OperatingSystemMXBean osMXBean;
    private Map<Integer, Long> topThreadIds = new HashMap<>();
    private Map<Integer, Long> blockedThreadIds = new HashMap<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private ThreadHelper threadHelper;
    private final int MAX_THREAD_NAME_LENGTH = 64;
    private String mainScreenId;
    private DisplayConfig displayConfig;

    public ConsoleController ( ConsoleReader consoleReader, UserInput userInput, MBeanServerConnection mbsc, DisplayConfig displayConfig ) {

        this.consoleReader = consoleReader;
        this.userInput = userInput;
        this.mbsc = mbsc;
        this.displayConfig = displayConfig;
        int displayThreadCount = displayConfig.getThreadCount();
        try {
            this.threadHelper = new ThreadHelper( mbsc );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // create GCStats objectName
        ObjectName gcStatsObjectName = null;
        try {
            gcStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.GC_STATS_TYPE );
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // instantiate new gcStatsMXBean proxy based on gcStatsObjectName
        this.gcStatsMXBean = JMX.newMBeanProxy(localMBS, gcStatsObjectName, GCStatsMXBean.class);

        // create MemoryStats objectName
        ObjectName memoryStatsObjectName = null;
        try {
            memoryStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.MEMORY_STATS_TYPE );
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // instantiate new memoryStatsMXBean proxy based on memoryStatsObjectName
        this.memoryStatsMXBean = JMX.newMBeanProxy(localMBS, memoryStatsObjectName, MemoryStatsMXBean.class);

        // create HeapStats objectName
        ObjectName heapStatsObjectName = null;
        try {
            heapStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.HEAP_STATS_TYPE );
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // instantiate new heapStatsMXBean proxy based on heapStatsObjectName
        this.heapStatsMXBean = JMX.newMBeanProxy(localMBS, heapStatsObjectName, HeapStatsMXBean.class);

        // create ThreadStats objectName
        ObjectName threadStatsObjectName = null;
        try {
            threadStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.THREADS_STATS_TYPE );
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // instantiate new threadStatsMXBean proxy based on threadStatsObjectName
        this.threadStatsMXBean = JMX.newMBeanProxy(localMBS, threadStatsObjectName, ThreadStatsMXBean.class);

        // populate topThread MBean list
        for (int rank =1; rank <=displayThreadCount; rank++) {

            // create TopThread objectName
            ObjectName topThreadObjectName = null;
            try {
                topThreadObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.TOP_THREAD_STATS_TYPE + ",rank=" + rank);
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
                System.exit(1);
            }
            // instantiate and store topThreadMXBean proxy based on topThreadObjectName
            this.topThreadMXBeans.add(JMX.newMBeanProxy(localMBS, topThreadObjectName, TopThreadMXBean.class));
        }

        // populate blockedThread MBean list
        for (int rank =1; rank <=displayThreadCount; rank++) {

            // create BlockedThread objectName
            ObjectName blockedThreadObjectName = null;
            try {
                blockedThreadObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.BLOCKED_THREAD_STATS_TYPE + ",rank=" + rank);
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
                System.exit(1);
            }
            // instantiate and store blockedThreadMXBean proxy based on blockedThreadObjectName
            this.blockedThreadMXBeans.add(JMX.newMBeanProxy(localMBS, blockedThreadObjectName, BlockedThreadMXBean.class));
        }

        // create ThreadMXBean objectName
        ObjectName threadMXBeanObjectName = null;
        try {
            threadMXBeanObjectName = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // instantiate new threadMXBean proxy based on threadMXBeanObjectName
        this.threadMXBean = JMX.newMBeanProxy(mbsc, threadMXBeanObjectName, ThreadMXBean.class);

        // create RuntimeMXBean objectName
        ObjectName runtimeMXBeanObjectName = null;
        try {
            runtimeMXBeanObjectName = new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // instantiate new runtimeMXBean proxy based on runtimeMXBeanObjectName
        this.runtimeMXBean = JMX.newMBeanProxy(mbsc, runtimeMXBeanObjectName, RuntimeMXBean.class);

        // create RuntimeMXBean objectName
        ObjectName osMXBeanObjectName = null;
        try {
            osMXBeanObjectName = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // instantiate new osMXBean proxy based on osMXBeanObjectName
        this.osMXBean = JMX.newMBeanProxy(mbsc, osMXBeanObjectName, OperatingSystemMXBean.class);

    }

    @Override
    public void run() {

        // first things first, check MBean Server Connection is still alive
        if (! isMBSConnectionAlive()) {
            // the connection to the remote MBean Server has been lost - we have no choice but to exit gracefully
            System.err.println("ERROR: The connection to the remote MBean Server has been lost. Check if the target JVM is still running.");
            System.exit(-1);
        }
        // retrieve user provided screenId from userInput shared object
        String screenId = this.userInput.getText();
        // store mainScreenId
        mainScreenId = screenId;
        String screen;
        if ( userInput.isDigit() ) {
            // create thread stack trace screen
            screen = createThreadStackTraceScreen(Integer.valueOf(screenId).intValue());
        }
        else if ( userInput.getText().equals("b") ) {
            // store mainScreenId
            mainScreenId = screenId;
            // create blocked threads screen
            screen = createBlockedThreadsScreen();
        }
        else {
            // store mainScreenId
            mainScreenId = screenId;
            // create top threads screen
            screen = createTopThreadsScreen();
        }
        try {
            // print screen
            consoleReader.clearScreen();
            consoleReader.println(screen);
            consoleReader.println();
            //consoleReader.println("Terminal Width: " + new Integer(consoleReader.getTerminal().getWidth()).toString());
            //consoleReader.println("Terminal Height: " + new Integer(consoleReader.getTerminal().getHeight()).toString());
            //consoleReader.println();
            //consoleReader.println("Test text: " + userInput.getText());
            //consoleReader.println();
            consoleReader.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private String createTop4JHeader() {

        Date date = new Date();
        StringBuffer sb = new StringBuffer();
        sb.append("top4j - " + timeFormat.format(date) + " up " + getUptime() + ",  load average: " + osMXBean.getSystemLoadAverage() + "\n");
        sb.append("Attached to: " + displayConfig.getDisplayName() + " [PID=" + displayConfig.getJvmPid() + "]" + "\n");
        sb.append("Threads: " + threadStatsMXBean.getThreadCount() + " total,   " +
                threadStatsMXBean.getRunnableThreadCount() + " runnable,   " +
                threadStatsMXBean.getWaitingThreadCount() + " waiting,   " +
                threadStatsMXBean.getTimedWaitingThreadCount() + " timed waiting,   " +
                threadStatsMXBean.getBlockedThreadCount() + " blocked\n");
        sb.append("%Cpu(s): " + String.format("%.2f", threadStatsMXBean.getCpuUsage()) + " total,  " +
                String.format("%.2f", threadStatsMXBean.getUserCpuUsage()) + " user,  " +
                String.format("%.2f", threadStatsMXBean.getSysCpuUsage()) + " sys\n");
        sb.append("Heap Util(%):        " + String.format("%.2f", heapStatsMXBean.getEdenSpaceUtil()) + " eden,        " +
                String.format("%.2f", heapStatsMXBean.getSurvivorSpaceUtil()) + " survivor,        " +
                String.format("%.2f", heapStatsMXBean.getTenuredHeapUtil()) + " tenured\n");
        sb.append("Mem Alloc(MB/s):     " + String.format("%.2f", memoryStatsMXBean.getMemoryAllocationRate()) + " eden,        " +
                String.format("%.2f", memoryStatsMXBean.getMemorySurvivorRate()) + " survivor,        " +
                String.format("%.2f", memoryStatsMXBean.getMemoryPromotionRate()) + " tenured\n");
        sb.append("GC Overhead(%):      " + String.format("%.4f", gcStatsMXBean.getGcOverhead()) + "\n");

        return sb.toString();
    }

    private String createTopThreadsScreen() {

        StringBuffer sb = new StringBuffer();
        sb.append(createTop4JHeader());
        sb.append("\n");
        sb.append("Top Threads:\n");
        sb.append("\n");
        sb.append("#  TID     S  %CPU  THREAD NAME\n");
        sb.append("=  ===     =  ====  ===========\n");
        // initialise thread counter
        int counter = 0;
        for (TopThreadMXBean topThreadMXBean : topThreadMXBeans) {

            String threadName = topThreadMXBean.getThreadName();
            // check we've got a top thread, continue to next topThreadMXBean if not
            if (threadName == null) {
                continue;
            }
            Long threadId = topThreadMXBean.getThreadId();
            String threadState = abbreviateThreadState( threadHelper.getThreadState( threadId ));
            Double threadCpuUsage = topThreadMXBean.getThreadCpuUsage();
            if (threadName != null && threadName.length() > MAX_THREAD_NAME_LENGTH) {
               threadName = threadName.substring(0, MAX_THREAD_NAME_LENGTH-1);
            }
            sb.append(  counter + "  " +
                    String.format("%1$-8s", threadId ) +
                    String.format("%1$-3s", threadState ) +
                    String.format("%1$-6.1f", threadCpuUsage ) +
                    String.format("%1$-64s", threadName ) +
                    "\n");

            // store thread Id
            topThreadIds.put(counter, topThreadMXBean.getThreadId());
            // increment thread counter
            counter++;
        }
        sb.append("\n\n");
        sb.append("Hit [0-9] to view thread stack trace, [b] to view blocked threads, [q] to quit\n");

        return sb.toString();

    }

    private String createBlockedThreadsScreen() {

        StringBuffer sb = new StringBuffer();
        sb.append(createTop4JHeader());
        sb.append("\n");
        sb.append("Blocked Threads:\n");
        sb.append("\n");
        sb.append("#  TID     S  %BLOCKED  THREAD NAME\n");
        sb.append("=  ===     =  ========  ===========\n");

        // first things first, check if there have been any blocked threads during this sample period
        Double totalThreadBlockedPercentage = 0.0;
        for (BlockedThreadMXBean blockedThreadMXBean : blockedThreadMXBeans) {
            totalThreadBlockedPercentage += blockedThreadMXBean.getThreadBlockedPercentage();
        }
        // if totalThreadBlockedPercentage is equal to zero, return "no blocked threads" message
        if (totalThreadBlockedPercentage == 0.0) {
            sb.append("\n");
            sb.append("No blocked threads detected.\n");
            sb.append("\n\n");
            sb.append("Hit [0-9] to view thread stack trace, [t] to view top threads, [q] to quit\n");

            return sb.toString();
        }

        // initialise thread counter
        int counter = 0;
        for (BlockedThreadMXBean blockedThreadMXBean : blockedThreadMXBeans) {

            String threadName = blockedThreadMXBean.getThreadName();
            // check we've got a blocked thread, continue to next blockedThreadMXBean if not
            if (threadName == null) {
                continue;
            }
            Long threadId = blockedThreadMXBean.getThreadId();
            String threadState = abbreviateThreadState( threadHelper.getThreadState( threadId ));
            Double threadBlockedPercentage = blockedThreadMXBean.getThreadBlockedPercentage();
            if (threadName != null && threadName.length() > MAX_THREAD_NAME_LENGTH) {
                threadName = threadName.substring(0, MAX_THREAD_NAME_LENGTH - 1);
            }
            sb.append(counter + "  " +
                    String.format("%1$-8s", threadId ) +
                    String.format("%1$-3s", threadState ) +
                    String.format("%1$-10.1f", threadBlockedPercentage ) +
                    String.format("%1$-50s", threadName ) +
                    "\n");

            // store thread Id
            blockedThreadIds.put(counter, blockedThreadMXBean.getThreadId());
            // increment thread counter
            counter++;
        }
        sb.append("\n\n");
        sb.append("Hit [0-9] to view thread stack trace, [t] to view top threads, [q] to quit\n");

        return sb.toString();

    }
    private String createThreadStackTraceScreen( int threadNumber ) {

        Date date = new Date();
        StringBuffer sb = new StringBuffer();
        long threadId;
        if (mainScreenId.equals("b")) {
            threadId = blockedThreadIds.get(threadNumber);
            BlockedThreadMXBean blockedThreadMXBean = blockedThreadMXBeans.get(threadNumber);
        }
        else {
            threadId = topThreadIds.get(threadNumber);
            TopThreadMXBean topThreadMXBean = topThreadMXBeans.get(threadNumber);
        }
        sb.append("top4j - " + timeFormat.format(date) + " up " + getUptime() + ",  load average: " + osMXBean.getSystemLoadAverage() + "\n");
        sb.append("\n");
        sb.append(threadHelper.getStackTraceWithContext(threadId, 15));
        sb.append("\n\n");
        sb.append("Hit [m] to return to main screen, [q] to quit\n");

        return sb.toString();
    }

    private String getUptime( ) {
        Long uptimeSecs = runtimeMXBean.getUptime() / 1000;
        String uptime;
        if (uptimeSecs > 86400) {
            uptime = uptimeSecs / 86400 + " days";
        }
        else {
            uptime = uptimeSecs + " secs";
        }
        return uptime;
    }

    private String abbreviateThreadState( Thread.State state ) {

        if (state == null) return "X";
        String abbreviatedState;
        switch (state) {

            case NEW:
                abbreviatedState = "N";
                break;
            case RUNNABLE:
                abbreviatedState = "R";
                break;
            case BLOCKED:
                abbreviatedState = "B";
                break;
            case WAITING:
                abbreviatedState = "W";
                break;
            case TIMED_WAITING:
                abbreviatedState = "T";
                break;
            case TERMINATED:
                abbreviatedState = "X";
                break;
            default:
                abbreviatedState = "U";
                break;
        }
        return abbreviatedState;
    }

    /*
        Check MBean Server Connection is still alive
     */
    private boolean isMBSConnectionAlive() {

        boolean isAlive;
        try {
            // use OS MX Bean to retrieve OS name
            String osName = osMXBean.getName();
            isAlive = true;
        }
        catch (Exception e) {
           isAlive = false;
        }
        return isAlive;
    }

}
