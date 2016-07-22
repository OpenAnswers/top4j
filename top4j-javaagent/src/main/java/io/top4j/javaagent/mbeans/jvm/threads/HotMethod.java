package io.top4j.javaagent.mbeans.jvm.threads;

import io.top4j.javaagent.utils.ThreadHelper;

import javax.management.MBeanServerConnection;
import java.io.IOException;

/**
 * Created by ryan on 27/07/15.
 */
public class HotMethod implements HotMethodMXBean {

    volatile private String methodName;
    volatile private String threadName;
	volatile private long threadId;
    volatile private double loadProfile;
    volatile private StackTraceElement[] stackTrace;
    private ThreadHelper threadHelper;

    public HotMethod( MBeanServerConnection mbsc ) throws IOException {

        this.threadHelper = new ThreadHelper( mbsc );

    }

    @Override
    public void setMethodName(String methodName) {
       this.methodName = methodName;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    @Override
    public long getThreadId() {
        return threadId;
    }

    @Override
    public void setLoadProfile(double loadProfile) {
       this.loadProfile = loadProfile;
    }

    @Override
    public double getLoadProfile() {
        return loadProfile;
    }

    @Override
    public String getStackTrace(int maxDepth) {
        return threadHelper.getStackTraceAsString(stackTrace);
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
       this.stackTrace = stackTrace;
    }
}