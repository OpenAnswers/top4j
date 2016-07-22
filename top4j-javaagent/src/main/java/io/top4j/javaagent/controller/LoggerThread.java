package io.top4j.javaagent.controller;

import io.top4j.javaagent.config.Configurator;
import io.top4j.javaagent.config.Constants;
import io.top4j.javaagent.mbeans.logger.StatsLoggerMXBean;
import io.top4j.javaagent.messaging.LoggerQueue;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

/**
 * Created by ryan on 07/09/15.
 */
public class LoggerThread extends Thread {

    private LoggerQueue loggerQueue;
    private long interval;
    private boolean statsLoggerEnabled;
    private StatsLoggerMXBean statsLogger;

    private static final Logger LOGGER = Logger.getLogger(LoggerThread.class.getName());

    public LoggerThread(Configurator config, LoggerQueue loggerQueue) throws Exception {

        super("Top4J Stats LoggerThread");
        LOGGER.fine("Initialising Top4J Stats LoggerThread....");
        this.loggerQueue = loggerQueue;
        this.interval = Long.parseLong(config.get("stats.logger.poll.timeout"));
        this.statsLoggerEnabled = config.isStatsLoggerEnabled();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        // create StatsLogger objectName
        ObjectName statsLoggerObjectName = null;
        try {
            statsLoggerObjectName = new ObjectName(Constants.DOMAIN + ":" + "type=" + Constants.AGENT_TYPE + ",statsType=" + Constants.STATS_LOGGER_TYPE);
        } catch (MalformedObjectNameException e) {
            throw new Exception( "JMX MalformedObjectNameException: " + e.getMessage() );
        }
        // instantiate new statsLoggerMXBean proxy based on statsLoggerObjectName
        this.statsLogger = JMX.newMBeanProxy(mbs, statsLoggerObjectName, StatsLoggerMXBean.class);
        setDaemon( true );
    }

    /** Run the thread until interrupted. */
    @Override
    public void run( ) {
        while ( !isInterrupted( ) ) {
            // poll loggerQueue for log stats notification
            String notification = loggerQueue.poll(interval);
            if (notification != null) {
                if (statsLoggerEnabled) {
                    // log JVM stats
                    statsLogger.update();
                }
            }
        }
    }

}
