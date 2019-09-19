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

import io.top4j.cli.exception.ScreenUpdateException;
import io.top4j.javaagent.config.Constants;
import io.top4j.javaagent.mbeans.StatsMXBean;
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
import java.util.logging.Logger;

public class ConsoleController  extends TimerTask {

    private final ConsoleReader consoleReader;
    private final UserInput userInput;
    private final MBeanServer localMBS = ManagementFactory.getPlatformMBeanServer();
    private final GCStatsMXBean gcStatsMXBean;
    private final MemoryStatsMXBean memoryStatsMXBean;
    private final HeapStatsMXBean heapStatsMXBean;
    private final ThreadStatsMXBean threadStatsMXBean;
    private List<TopThreadMXBean> topThreadMXBeans = new ArrayList<>();
    private List<BlockedThreadMXBean> blockedThreadMXBeans = new ArrayList<>();
    private List<StatsMXBean> jvmStatsMBeans = new ArrayList<>();
    private final RuntimeMXBean runtimeMXBean;
    private final OperatingSystemMXBean osMXBean;
    private Map<Integer, Long> topThreadIds = new HashMap<>();
    private Map<Integer, Long> blockedThreadIds = new HashMap<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private ThreadHelper threadHelper;
    private final static int MAX_THREAD_NAME_LENGTH = 64;
    private final static int MAX_DISPLAY_NAME_LENGTH = 64;
    private String mainScreenId;
    private DisplayConfig displayConfig;
    private static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String INIT_ERROR_MESSAGE = "A problem occurred attempting to initialise Top4J CLI Console Controller: ";

    private static final Logger LOGGER = Logger.getLogger(ConsoleController.class.getName());

    public ConsoleController ( ConsoleReader consoleReader, UserInput userInput, MBeanServerConnection mbsc, DisplayConfig displayConfig ) {

        this.consoleReader = consoleReader;
        this.userInput = userInput;
        this.displayConfig = displayConfig;
        int displayThreadCount = displayConfig.getThreadCount();
        try {
            this.threadHelper = new ThreadHelper( mbsc );
        } catch (IOException e) {
            String errorMessage = INIT_ERROR_MESSAGE + e.getMessage();
            LOGGER.severe(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }

        // create GCStats objectName
        ObjectName gcStatsObjectName = null;
        try {
            gcStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.GC_STATS_TYPE );
        } catch (MalformedObjectNameException e) {
            String errorMessage = INIT_ERROR_MESSAGE + e.getMessage();
            LOGGER.severe(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
        // instantiate new gcStatsMXBean proxy based on gcStatsObjectName
        this.gcStatsMXBean = JMX.newMBeanProxy(localMBS, gcStatsObjectName, GCStatsMXBean.class);
        // add gcStatsMXBean proxy to list of jvmStatsMBeans
        this.jvmStatsMBeans.add(this.gcStatsMXBean);

        // create MemoryStats objectName
        ObjectName memoryStatsObjectName = null;
        try {
            memoryStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.MEMORY_STATS_TYPE );
        } catch (MalformedObjectNameException e) {
            String errorMessage = INIT_ERROR_MESSAGE + e.getMessage();
            LOGGER.severe(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
        // instantiate new memoryStatsMXBean proxy based on memoryStatsObjectName
        this.memoryStatsMXBean = JMX.newMBeanProxy(localMBS, memoryStatsObjectName, MemoryStatsMXBean.class);
        // add memoryStatsMXBean proxy to list of jvmStatsMBeans
        this.jvmStatsMBeans.add(this.memoryStatsMXBean);

        // create HeapStats objectName
        ObjectName heapStatsObjectName = null;
        try {
            heapStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.HEAP_STATS_TYPE );
        } catch (MalformedObjectNameException e) {
            String errorMessage = INIT_ERROR_MESSAGE + e.getMessage();
            LOGGER.severe(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
        // instantiate new heapStatsMXBean proxy based on heapStatsObjectName
        this.heapStatsMXBean = JMX.newMBeanProxy(localMBS, heapStatsObjectName, HeapStatsMXBean.class);
        // add heapStatsMXBean proxy to list of jvmStatsMBeans
        this.jvmStatsMBeans.add(this.heapStatsMXBean);

        // create ThreadStats objectName
        ObjectName threadStatsObjectName = null;
        try {
            threadStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.THREADS_STATS_TYPE );
        } catch (MalformedObjectNameException e) {
            String errorMessage = INIT_ERROR_MESSAGE + e.getMessage();
            LOGGER.severe(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
        // instantiate new threadStatsMXBean proxy based on threadStatsObjectName
        this.threadStatsMXBean = JMX.newMBeanProxy(localMBS, threadStatsObjectName, ThreadStatsMXBean.class);
        // add threadStatsMXBean proxy to list of jvmStatsMBeans
        this.jvmStatsMBeans.add(this.threadStatsMXBean);

        // populate topThread MBean list
        for (int rank =1; rank <=displayThreadCount; rank++) {

            // create TopThread objectName
            ObjectName topThreadObjectName = null;
            try {
                topThreadObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.TOP_THREAD_STATS_TYPE + ",rank=" + rank);
            } catch (MalformedObjectNameException e) {
                String errorMessage = INIT_ERROR_MESSAGE + e.getMessage();
                LOGGER.severe(errorMessage);
                throw new IllegalStateException(errorMessage, e);
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
                String errorMessage = INIT_ERROR_MESSAGE + e.getMessage();
                LOGGER.severe(errorMessage);
                throw new IllegalStateException(errorMessage, e);
            }
            // instantiate and store blockedThreadMXBean proxy based on blockedThreadObjectName
            this.blockedThreadMXBeans.add(JMX.newMBeanProxy(localMBS, blockedThreadObjectName, BlockedThreadMXBean.class));
        }

        // create RuntimeMXBean objectName
        ObjectName runtimeMXBeanObjectName = null;
        try {
            runtimeMXBeanObjectName = new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            String errorMessage = INIT_ERROR_MESSAGE + e.getMessage();
            LOGGER.severe(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
        // instantiate new runtimeMXBean proxy based on runtimeMXBeanObjectName
        this.runtimeMXBean = JMX.newMBeanProxy(mbsc, runtimeMXBeanObjectName, RuntimeMXBean.class);

        // create OperatingSystemMXBean objectName
        ObjectName osMXBeanObjectName = null;
        try {
            osMXBeanObjectName = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            String errorMessage = INIT_ERROR_MESSAGE + e.getMessage();
            LOGGER.severe(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
        // instantiate new osMXBean proxy based on osMXBeanObjectName
        this.osMXBean = JMX.newMBeanProxy(mbsc, osMXBeanObjectName, OperatingSystemMXBean.class);

    }

    @Override
    public void run() {

        try {
            // update console screen
            updateScreen();

        } catch (ScreenUpdateException e) {
            // something went wrong - print error message and exit gracefully
            LOGGER.severe(e.getMessage());
            System.exit(1);
        }

    }

    private void updateScreen() throws ScreenUpdateException {

        // first things first, check MBean Server Connection is still alive
        if (! isMBSConnectionAlive()) {
            // the connection to the remote MBean Server has been lost - we have no choice but to exit gracefully
            throw new ScreenUpdateException("ERROR: The connection to the remote MBean Server has been lost. Check if the target JVM is still running.");
        }
        // now check the Top4J Java Agent status
        checkJavaAgentStatus();
        // retrieve user provided screenId from userInput shared object
        String screenId = this.userInput.getScreenId();
        // retrieve user provided userText from userInput shared object
        String userText = this.userInput.getText();
        // store mainScreenId
        mainScreenId = screenId;
        String screen;
        try {
            if ( userInput.isDigit() ) {
                // create thread stack trace screen
                screen = createThreadStackTraceScreen(Integer.valueOf(userText).intValue());
            } else if ( screenId.equals("b") ) {
                // create blocked threads screen
                screen = createBlockedThreadsScreen();
            } else {
                // create top threads screen
                screen = createTopThreadsScreen();
            }
            // print screen
            consoleReader.clearScreen();
            consoleReader.println(screen);
            consoleReader.println();
            consoleReader.flush();
        } catch (Exception e) {
            // throw ScreenUpdateException
            throw new ScreenUpdateException("ERROR: A problem occurred attempting to refresh Top4J CLI Console: " + e.getMessage());
        }
    }

    private String createTop4JHeader() {

        Date date = new Date();
        StringBuilder sb = new StringBuilder();
        String displayName = displayConfig.getDisplayName();
        if (displayName != null && displayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            displayName = displayName.substring(0, MAX_DISPLAY_NAME_LENGTH-1);
        }
        sb.append("top4j - " + timeFormat.format(date) + " up " + getUptime() + ",  load average: " + osMXBean.getSystemLoadAverage() + "\n");
        sb.append("Attached to: " + displayName + " [PID=" + displayConfig.getJvmPid() + "]" + "\n");
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

        StringBuilder sb = new StringBuilder();
        sb.append(createTop4JHeader());
        sb.append("\n");
        sb.append("TOP THREADS:\n");
        sb.append("\n");
        sb.append(highlightHeading("#  TID     S  %CPU  THREAD NAME"));
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

        StringBuilder sb = new StringBuilder();
        sb.append(createTop4JHeader());
        sb.append("\n");
        sb.append("BLOCKED THREADS:\n");
        sb.append("\n");
        sb.append(highlightHeading("#  TID     S  %BLOCKED  THREAD NAME"));

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
        StringBuilder sb = new StringBuilder();
        long threadId;
        if (mainScreenId.equals("b")) {
            threadId = blockedThreadIds.get(threadNumber);
        } else {
            threadId = topThreadIds.get(threadNumber);
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
        } else {
            uptime = uptimeSecs + " secs";
        }
        return uptime;
    }

    private String abbreviateThreadState( Thread.State state ) {

        if (state == null) {
            return "X";
        }
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
            osMXBean.getName();
            isAlive = true;
        }
        catch (Exception e) {
           isAlive = false;
        }
        return isAlive;
    }

    /**
     *
     *   Highlight heading using ANSI escape characters
     */

    private String highlightHeading( String heading ) {
        int consoleWidth = consoleReader.getTerminal().getWidth();
        StringBuilder sb = new StringBuilder();
        // prepend heading with ANSI escape characters
        sb.append(ANSI_WHITE_BACKGROUND + ANSI_BLACK + heading);
        if (consoleWidth > heading.length()) {
            // pad heading to consoleWidth
            for (int i = heading.length(); i < consoleWidth; i++) {
                sb.append(" ");
            }
        }
        // reset text highlighting at end of heading
        sb.append(ANSI_RESET);
        return sb.toString();

    }

    /**
     * Check if Top4J Java Agent is OK via JVM Stats MBean Enabled attribute
     * @throws ScreenUpdateException If it detects a problem with the Top4J Java Agent
     */
    private void checkJavaAgentStatus( ) throws ScreenUpdateException {

        // check jvmStats
        for (StatsMXBean jvmStats : jvmStatsMBeans) {
            // check jvmStats status via MBean Enabled attribute
            if (!jvmStats.getEnabled()) {
                // extract MBean Object Name from MBean proxy
                String jvmStatsToString = jvmStats.toString();
                String jvmStatsObjectName = jvmStatsToString.substring(jvmStatsToString.indexOf("[")+1,jvmStatsToString.indexOf("]"));
                // throw ScreenUpdateException if any of the jvmStats MBeans are disabled
                throw new ScreenUpdateException("ERROR: The Top4J Java Agent encountered a problem updating MBean [" + jvmStatsObjectName + "] due to: " + jvmStats.getFailureReason() );
            }
        }
    }

}
