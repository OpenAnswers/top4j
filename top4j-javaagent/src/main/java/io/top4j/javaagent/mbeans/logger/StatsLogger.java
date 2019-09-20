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

package io.top4j.javaagent.mbeans.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.management.*;

import io.top4j.javaagent.config.Configurator;
import io.top4j.javaagent.config.Constants;
import io.top4j.javaagent.exception.MBeanInitException;
import io.top4j.javaagent.mbeans.jvm.threads.BlockedThreadMXBean;
import io.top4j.javaagent.mbeans.jvm.threads.HotMethodMXBean;
import io.top4j.javaagent.mbeans.jvm.threads.TopThreadMXBean;
import io.top4j.javaagent.profiler.CpuTime;
import io.top4j.javaagent.utils.MBeanInfo;

public final class StatsLogger implements StatsLoggerMXBean {

    volatile private double mBeanCpuTime;
    private boolean statsLoggerEnabled;
    private String statsLoggerFormat;
    private String statsLoggerDirectory;
    private String statsLoggerDateFormat;
    private String statsLoggerFieldSeparator;
    private String dateStampFormat;
    private boolean topThreadsStackTraceLoggingEnabled;
    private int topThreadsStackTraceCpuThreshold;
    private int topThreadsStackTraceFrames;
    private Map<String, TopThreadMXBean> topThreadsMXBeans;
    private boolean blockedThreadsStackTraceLoggingEnabled;
    private int blockedThreadsStackTraceThreshold;
    private int blockedThreadsStackTraceFrames;
    private Map<String, BlockedThreadMXBean> blockedThreadsMXBeans;
    private boolean hotMethodsStackTraceLoggingEnabled;
    private int hotMethodStackTraceLoadProfileThreshold;
    private int hotMethodStackTraceFrames;
    private Map<String, HotMethodMXBean> hotMethodsMXBeans;
    private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private List<MBeanInfo> mbeanInfoList;
    private String lastDateStamp;
    private Map<String, StatsLogWriter> statsLogWriter = new HashMap<>();
    private CpuTime cpuTime = new CpuTime();
    private boolean enabled = true;
    private String failureReason;

    private static final Logger LOGGER = Logger.getLogger(StatsLogger.class.getName());

    public StatsLogger(Configurator config) throws Exception {
        LOGGER.fine("Initialising Top4J Stats LoggerThread MBean....");
        this.statsLoggerEnabled = config.isStatsLoggerEnabled();
        this.statsLoggerFormat = config.get("stats.logger.format");
        this.statsLoggerDirectory = config.get("stats.logger.directory");
        this.statsLoggerDateFormat = config.get("stats.logger.date.format");
        this.statsLoggerFieldSeparator = config.get("stats.logger.field.separator");
        this.dateStampFormat = config.get("stats.logger.log.file.name.date.format");
        this.topThreadsStackTraceLoggingEnabled = config.isTopThreadsStackTraceLoggingEnabled();
        this.topThreadsStackTraceCpuThreshold = Integer.parseInt(config.get("top.threads.stack.trace.cpu.threshold"));
        this.topThreadsStackTraceFrames = Integer.parseInt(config.get("top.threads.stack.trace.frames"));
        this.blockedThreadsStackTraceLoggingEnabled = config.isBlockedThreadsStackTraceLoggingEnabled();
        this.blockedThreadsStackTraceThreshold = Integer.parseInt(config.get("blocked.threads.stack.trace.blocked.percentage.threshold"));
        this.blockedThreadsStackTraceFrames = Integer.parseInt(config.get("blocked.threads.stack.trace.frames"));
        this.hotMethodsStackTraceLoggingEnabled = config.isHotMethodStackTraceLoggingEnabled();
        this.hotMethodStackTraceLoadProfileThreshold = Integer.parseInt(config.get("hot.method.stack.trace.load.profile.percentage.threshold"));
        this.hotMethodStackTraceFrames = Integer.parseInt(config.get("hot.method.stack.trace.frames"));
        this.mbeanInfoList = new ArrayList<>();
        // create statsLoggerDirectory
        createLogDirectory(statsLoggerDirectory);
        // get current dateStamp
        String dateStamp = new SimpleDateFormat(dateStampFormat).format(new Date());
        this.lastDateStamp = dateStamp;
        ObjectName top4jStatsName = null;
        try {
            top4jStatsName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.JVM_STATS_TYPE + ",*");
        } catch (MalformedObjectNameException e) {
            throw new MBeanInitException(e, "JMX MalformedObjectNameException: " + e.getMessage());
        }
        Set<ObjectName> top4jMBeans = mbs.queryNames(top4jStatsName, null);
        for (ObjectName top4jMbean : top4jMBeans) {

            MBeanAttributeInfo[] top4jMbeanAttributes = null;
            LOGGER.fine("Registering Top4J MBean with Stats Logger: " + top4jMbean.getCanonicalName());

            try {
                top4jMbeanAttributes = mbs.getMBeanInfo(top4jMbean).getAttributes();

            } catch (IntrospectionException e) {
                throw new MBeanInitException(e, "JMX IntrospectionException: " + e.getMessage());
            } catch (InstanceNotFoundException e) {
                throw new MBeanInitException(e, "JMX InstanceNotFoundException: " + e.getMessage());
            } catch (ReflectionException e) {
                throw new MBeanInitException(e, "JMX ReflectionException: " + e.getMessage());
            }
            // instantiate new MBean info bean
            MBeanInfo mbeanInfo = new MBeanInfo();
            // store MBean ObjectName
            mbeanInfo.setObjectName(top4jMbean);
            // store this MBean key property list
            String keyPropertyList = top4jMbean.getKeyPropertyListString();
            mbeanInfo.setKeyPropertyList(keyPropertyList);
            // instantiate new TreeSet to store mbeanAttributeNames
            Collection<String> mbeanAttributeNames =
                    new TreeSet<String>(Collator.getInstance());

            for (MBeanAttributeInfo top4jMbeanAttribute : top4jMbeanAttributes) {

                // add MBean attribute name to mbeanAttributeNames collection
                mbeanAttributeNames.add(top4jMbeanAttribute.getName());

            }
            // store this MBean attribute list
            mbeanInfo.setAttributeNames(mbeanAttributeNames);
            // extract statsType from keyPropertyList, e.g. type=JVM,statsType=ThreadStats
            String statsType = keyPropertyList.split(",")[1].split("=")[1];
            // store this statsType, e.g. type=JVM,statsType=TopThread,rank=1
            if (statsType.equals("TopThread")) {
                String rank = keyPropertyList.split(",")[2].split("=")[1];
                String topThreadsStatsType = statsType + "-" + rank;
                mbeanInfo.setStatsType(topThreadsStatsType);
                // initialise stats log file
                initStatsLogFile(topThreadsStatsType, dateStamp, mbeanAttributeNames);
                if (topThreadsStackTraceLoggingEnabled && rank.equals("1")) {
                    // initialise stack trace log file
                    initStackTraceLogFile(statsType, dateStamp);
                }
            }
            // store this statsType, e.g. type=JVM,statsType=BlockedThread,rank=1
            else if (statsType.equals("BlockedThread")) {
                String rank = keyPropertyList.split(",")[2].split("=")[1];
                String blockedThreadsStatsType = statsType + "-" + rank;
                mbeanInfo.setStatsType(blockedThreadsStatsType);
                // initialise stats log file
                initStatsLogFile(blockedThreadsStatsType, dateStamp, mbeanAttributeNames);
                if (blockedThreadsStackTraceLoggingEnabled && rank.equals("1")) {
                    // initialise stack trace log file
                    initStackTraceLogFile(statsType, dateStamp);
                }
            }
            // store this statsType, e.g. type=JVM,statsType=HotMethod,rank=1
            else if (statsType.equals("HotMethod")) {
                String rank = keyPropertyList.split(",")[2].split("=")[1];
                String hotMethodsStatsType = statsType + "-" + rank;
                mbeanInfo.setStatsType(hotMethodsStatsType);
                // initialise stats log file
                initStatsLogFile(hotMethodsStatsType, dateStamp, mbeanAttributeNames);
                if (hotMethodsStackTraceLoggingEnabled && rank.equals("1")) {
                    // initialise stack trace log file
                    initStackTraceLogFile(statsType, dateStamp);
                }
            } else {
                mbeanInfo.setStatsType(statsType);
                // initialise stats log file
                initStatsLogFile(statsType, dateStamp, mbeanAttributeNames);
            }

            // store this MBean info bean
            this.mbeanInfoList.add(mbeanInfo);
        }
        if (topThreadsStackTraceLoggingEnabled) {

            int topThreadCount = Integer.parseInt(config.get("top.thread.count"));

            this.topThreadsMXBeans = new HashMap<>();

            for (int rank = 1; rank <= topThreadCount; rank++) {

                // convert rank to String
                String ranking = String.valueOf(rank);
                // set Map key to MBean key property list
                String key = "type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.TOP_THREAD_STATS_TYPE + ",rank=" + ranking;
                // create TopThread objectName based on this type, statsType and rank
                ObjectName topThreadsObjectName = null;
                try {
                    topThreadsObjectName = new ObjectName(Constants.DOMAIN + ":" + key);
                } catch (MalformedObjectNameException e) {
                    throw new MBeanInitException(e, "JMX MalformedObjectNameException: " + e.getMessage());
                }
                // instantiate new topThreadMXBean proxy based on topThreadsObjectName
                TopThreadMXBean topThreadMXBean = JMX.newMBeanProxy(mbs, topThreadsObjectName, TopThreadMXBean.class);
                // add topThreadsMBeanHelper to list of topThreadsMBeanHelpers
                this.topThreadsMXBeans.put(key, topThreadMXBean);

            }

        }
        if (blockedThreadsStackTraceLoggingEnabled) {

            int blockedThreadCount = Integer.parseInt(config.get("blocked.thread.count"));

            this.blockedThreadsMXBeans = new HashMap<>();

            for (int rank = 1; rank <= blockedThreadCount; rank++) {

                // convert rank to String
                String ranking = String.valueOf(rank);
                // set Map key to MBean key property list
                String key = "type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.BLOCKED_THREAD_STATS_TYPE + ",rank=" + ranking;
                // create BlockedThread objectName based on this type, statsType and rank
                ObjectName blockedThreadsObjectName = null;
                try {
                    blockedThreadsObjectName = new ObjectName(Constants.DOMAIN + ":" + key);
                } catch (MalformedObjectNameException e) {
                    throw new MBeanInitException(e, "JMX MalformedObjectNameException: " + e.getMessage());
                }
                // instantiate new blockedThreadMXBean proxy based on blockedThreadsObjectName
                BlockedThreadMXBean blockedThreadMXBean = JMX.newMBeanProxy(mbs, blockedThreadsObjectName, BlockedThreadMXBean.class);
                // add blockedThreadsMBeanHelper to list of blockedThreadsMBeanHelpers
                this.blockedThreadsMXBeans.put(key, blockedThreadMXBean);

            }

        }
        if (hotMethodsStackTraceLoggingEnabled) {

            int hotMethodCount = Integer.parseInt(config.get("hot.method.count"));

            this.hotMethodsMXBeans = new HashMap<>();

            for (int rank = 1; rank <= hotMethodCount; rank++) {

                // convert rank to String
                String ranking = String.valueOf(rank);
                // set Map key to MBean key property list
                String key = "type=" + Constants.JVM_STATS_TYPE + ",statsType=" + Constants.HOT_METHOD_STATS_TYPE + ",rank=" + ranking;
                // create HotMethod objectName based on this type, statsType and rank
                ObjectName hotMethodsObjectName = null;
                try {
                    hotMethodsObjectName = new ObjectName(Constants.DOMAIN + ":" + key);
                } catch (MalformedObjectNameException e) {
                    throw new MBeanInitException(e, "JMX MalformedObjectNameException: " + e.getMessage());
                }
                // instantiate new hotMethodMXBean proxy based on hotMethodsObjectName
                HotMethodMXBean hotMethodMXBean = JMX.newMBeanProxy(mbs, hotMethodsObjectName, HotMethodMXBean.class);
                // add hotMethodsMBeanHelper to list of hotMethodsMBeanHelpers
                this.hotMethodsMXBeans.put(key, hotMethodMXBean);

            }

        }

    }

    @Override
    public synchronized void update() {

        if (enabled) {
            try {
                // update stats logger
                updateStatsLogger();
            } catch (Exception e) {
                // something went wrong - record failure reason and disable any further updates
                this.failureReason = e.getMessage();
                this.enabled = false;
                LOGGER.severe("TOP4J ERROR: Failed to update StatsLogger MBean due to: " + e.getMessage());
                LOGGER.severe("TOP4J ERROR: Further StatsLogger MBean updates will be disabled from now on.");
            }
        }
    }

    private synchronized void updateStatsLogger() {

        // initialise thread CPU timer
        cpuTime.init();

        // get current dateStamp
        String dateStamp = new SimpleDateFormat(dateStampFormat).format(new Date());
        // get current timestamp
        String timestamp = new SimpleDateFormat(statsLoggerDateFormat).format(new Date());
        // check if date has rolled since last iteration
        boolean rollStatsLogFile = false;
        if (!dateStamp.equals(lastDateStamp)) {
            // roll stats log file
            rollStatsLogFile = true;
        }

        for (MBeanInfo mbeanInfo : mbeanInfoList) {

            StringBuilder logEntry = new StringBuilder();

            // add timestamp to logEntry
            logEntry.append(timestamp);

            // get MBean key property list, e.g. type=JVM,statsType=ThreadStats
            String keyPropertyList = mbeanInfo.getKeyPropertyList();

            // get MBean statsType
            String statsType = mbeanInfo.getStatsType();
            LOGGER.fine("Logging stats for MBean stats type = " + statsType);

            if (statsLoggerFormat.equals("console")) {
                logEntry.append(",");
                logEntry.append(keyPropertyList);
            }

            ObjectName mbeanObjectName = mbeanInfo.getObjectName();
            Collection<String> mbeanAttributeNames = mbeanInfo.getAttributeNames();

            for (String mbeanAttributeName : mbeanAttributeNames) {

                Object mbeanAttribute = null;
                try {
                    mbeanAttribute = mbs.getAttribute(mbeanObjectName, mbeanAttributeName);
                } catch (AttributeNotFoundException | InstanceNotFoundException
                        | MBeanException | ReflectionException e) {
                    LOGGER.fine("Unable to retrieve " + mbeanObjectName.toString() + " MBean attribute " + mbeanAttributeName + " value due to: " + e.getMessage());
                }


                if (statsLoggerFormat.equals("csv")) {

                    if (mbeanAttribute instanceof Double) {
                        logEntry.append(statsLoggerFieldSeparator);
                        logEntry.append(String.format("%.4f", mbeanAttribute));
                    } else {
                        logEntry.append(statsLoggerFieldSeparator);
                        logEntry.append(mbeanAttribute);
                    }

                } else {
                    if (mbeanAttribute instanceof Double) {
                        logEntry.append(",");
                        logEntry.append(mbeanAttributeName);
                        logEntry.append("=");
                        logEntry.append(String.format("%.4f", mbeanAttribute));
                    } else {
                        logEntry.append(",");
                        logEntry.append(mbeanAttributeName);
                        logEntry.append("=");
                        logEntry.append(mbeanAttribute);
                    }
                }

            }

            if (statsLoggerFormat.equals("console")) {
                // log stats
                LOGGER.info(logEntry.toString());
            }

            if (statsLoggerFormat.equals("csv")) {
                // check if date has rolled since last iteration
                if (rollStatsLogFile) {
                    // reinitialise stats log file
                    initStatsLogFile(statsType, dateStamp, mbeanAttributeNames);
                }
                // log stats to file
                writeToFile(statsType, logEntry.toString());
            }

            if (topThreadsStackTraceLoggingEnabled) {

                if (statsType.startsWith(Constants.TOP_THREAD_STATS_TYPE)) {
                    // check if date has rolled since last iteration
                    if (rollStatsLogFile) {
                        // reinitialise stack trace log file
                        initStackTraceLogFile(Constants.TOP_THREAD_STATS_TYPE, dateStamp);
                    }
                    logTopThreadsStackTrace(keyPropertyList, timestamp);
                }
            }

            if (blockedThreadsStackTraceLoggingEnabled) {

                if (statsType.startsWith(Constants.BLOCKED_THREAD_STATS_TYPE)) {
                    // check if date has rolled since last iteration
                    if (rollStatsLogFile) {
                        // reinitialise stack trace log file
                        initStackTraceLogFile(Constants.BLOCKED_THREAD_STATS_TYPE, dateStamp);
                    }
                    logBlockedThreadsStackTrace(keyPropertyList, timestamp);
                }
            }

            if (hotMethodsStackTraceLoggingEnabled) {

                if (statsType.startsWith(Constants.HOT_METHOD_STATS_TYPE)) {
                    // check if date has rolled since last iteration
                    if (rollStatsLogFile) {
                        // reinitialise stack trace log file
                        initStackTraceLogFile(Constants.HOT_METHOD_STATS_TYPE, dateStamp);
                    }
                    logHotMethodsStackTrace(keyPropertyList, timestamp);
                }
            }

        }

        // update agent stats CPU time
        mBeanCpuTime = cpuTime.getMillis();

    }

    private synchronized void logTopThreadsStackTrace(String keyPropertyList, String timestamp) {

        TopThreadMXBean topThreadMXBean = topThreadsMXBeans.get(keyPropertyList);
        double threadCpuUsage = topThreadMXBean.getThreadCpuUsage();
        if (threadCpuUsage > topThreadsStackTraceCpuThreshold) {
            writeToFile(Constants.TOP_THREAD_STATS_TYPE,
                    timestamp + "," + keyPropertyList +
                            ",Thread CPU usage (" + String.format("%.2f", threadCpuUsage) + ") exceeded threshold (" +
                            topThreadsStackTraceCpuThreshold + ")");
            // log stack trace for this TopThread
            writeToFile(Constants.TOP_THREAD_STATS_TYPE,
                    topThreadsMXBeans.get(keyPropertyList).getStackTrace(topThreadsStackTraceFrames));
        }

    }

    private synchronized void logBlockedThreadsStackTrace(String keyPropertyList, String timestamp) {

        BlockedThreadMXBean blockedThreadMXBean = blockedThreadsMXBeans.get(keyPropertyList);
        double threadBlockedPercentage = blockedThreadMXBean.getThreadBlockedPercentage();
        if (threadBlockedPercentage > blockedThreadsStackTraceThreshold) {
            writeToFile(Constants.BLOCKED_THREAD_STATS_TYPE,
                    timestamp + "," + keyPropertyList +
                            ",Thread blocked time percentage (" + String.format("%.2f", threadBlockedPercentage) + ") exceeded threshold (" +
                            blockedThreadsStackTraceThreshold + ")");
            // log stack trace for this BlockedThread
            writeToFile(Constants.BLOCKED_THREAD_STATS_TYPE,
                    blockedThreadsMXBeans.get(keyPropertyList).getStackTrace(blockedThreadsStackTraceFrames));
        }

    }

    private synchronized void logHotMethodsStackTrace(String keyPropertyList, String timestamp) {

        HotMethodMXBean hotMethodMXBean = hotMethodsMXBeans.get(keyPropertyList);
        double hotMethodLoadProfile = hotMethodMXBean.getLoadProfile();
        if (hotMethodLoadProfile > hotMethodStackTraceLoadProfileThreshold) {
            writeToFile(Constants.HOT_METHOD_STATS_TYPE,
                    timestamp + "," + keyPropertyList +
                            ",Hot method load profile (" + String.format("%.2f", hotMethodLoadProfile) + ") exceeded threshold (" +
                            hotMethodStackTraceLoadProfileThreshold + ")");
            // log stack trace for this HotMethod
            writeToFile(Constants.HOT_METHOD_STATS_TYPE,
                    hotMethodsMXBeans.get(keyPropertyList).getStackTrace(hotMethodStackTraceFrames));
        }

    }

    private void createLogDirectory(String statsLoggerDirectory) {

        File statsDir = new File(statsLoggerDirectory);
        boolean createDirStatus = false;
        if (statsDir.exists() && statsDir.isFile()) {
            LOGGER.severe("The stats log directory " + statsLoggerDirectory + " could not be created as it is a normal file");
            this.statsLoggerEnabled = false;
        } else {
            if (!statsDir.exists()) {
                try {
                    createDirStatus = statsDir.mkdirs();
                } catch (SecurityException se) {
                    LOGGER.severe("Unable to create " + statsLoggerDirectory + ". Stats logging disabled!!");
                    this.statsLoggerEnabled = false;
                }
                if (!createDirStatus) {
                    LOGGER.severe("Unable to create " + statsLoggerDirectory + ". Stats logging disabled!!");
                    this.statsLoggerEnabled = false;
                }
            }
        }
        // check that the statsDir is writable
        if (!statsDir.canWrite()) {
            LOGGER.severe("Unable to write to " + statsLoggerDirectory + ". Stats logging disabled!!");
            this.statsLoggerEnabled = false;
        }
    }

    @SuppressWarnings("resource")
    private void initStatsLogFile(String statsType, String datestamp, Collection<String> mbeanAttributeNames) {

        // initialise stats log file
        String fileName = statsLoggerDirectory + Constants.FILE_SEPARATOR + statsType + "." + datestamp + "." + statsLoggerFormat;
        LOGGER.finest("Stats log file name: " + fileName);
        // get previous statsLogWriter (if it exists)
        StatsLogWriter statsLogWriter = this.statsLogWriter.get(statsType);
        if (statsLogWriter != null) {
            // close statsLogWriter
            statsLogWriter.close();
        }
        // generate CSV header string from mbeanAttributeNames
        String header = createCsvHeader(mbeanAttributeNames);
        // init new statsLogWriter
        try {
            statsLogWriter = new StatsLogWriter(fileName, header);
        } catch (Exception e) {
            LOGGER.severe("Unable to initialise stats log file " + fileName + " due to: " + e.getMessage());
            this.statsLoggerEnabled = false;
        }
        // store statsType statsLogWriter
        this.statsLogWriter.put(statsType, statsLogWriter);
    }

    @SuppressWarnings("resource")
    private void initStackTraceLogFile(String statsType, String datestamp) {

        // initialise stack trace log file
        String fileName = statsLoggerDirectory + Constants.FILE_SEPARATOR + statsType + ".StackTrace." + datestamp + ".log";
        LOGGER.finest("Stack trace log file name: " + fileName);
        // get previous statsLogWriter (if it exists)
        StatsLogWriter statsLogWriter = this.statsLogWriter.get(statsType);
        if (statsLogWriter != null) {
            // close statsLogWriter
            statsLogWriter.close();
        }
        // init new statsLogWriter
        try {
            statsLogWriter = new StatsLogWriter(fileName);
        } catch (Exception e) {
            LOGGER.severe("Unable to initialise stack trace log file " + fileName + " due to: " + e.getMessage());
            this.statsLoggerEnabled = false;
        }
        // store statsType statsLogWriter
        this.statsLogWriter.put(statsType, statsLogWriter);
    }

    private String createCsvHeader(Collection<String> mbeanAttributeNames) {

        StringBuilder header = new StringBuilder();
        header.append("Timestamp");
        for (String mbeanAttributeName : mbeanAttributeNames) {
            header.append(statsLoggerFieldSeparator);
            header.append(mbeanAttributeName);
        }
        return header.toString();
    }

    private synchronized void writeToFile(String statsType, String logEntry) {

        // write logEntry to stats log file via statsLogWriter
        this.statsLogWriter.get(statsType).println(logEntry);

    }

    @Override
    public void setMBeanCpuTime(double mBeanCpuTime) {
        this.mBeanCpuTime = mBeanCpuTime;
    }

    @Override
    public double getMBeanCpuTime() {
        return mBeanCpuTime;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean getEnabled() {
        return this.enabled;
    }

    @Override
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    @Override
    public String getFailureReason() {
        return this.failureReason;
    }

}
