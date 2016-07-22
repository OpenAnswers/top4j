package io.top4j.javaagent.mbeans.jvm.threads;

import io.top4j.javaagent.utils.ThreadHelper;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.Thread.State;
import java.lang.management.ThreadInfo;


public class TopThread implements TopThreadMXBean {
	
	volatile private String threadName;
	volatile private long threadId;
	volatile private State threadState;
	volatile private double threadCpuUsage;
	private ThreadHelper threadHelper;
    private ThreadInfo threadInfo;

	public TopThread( MBeanServerConnection mbsc ) throws IOException {

		this.threadHelper = new ThreadHelper( mbsc );

	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public long getThreadId() {
		return threadId;
	}

	public void setThreadState(State threadState) {
		this.threadState = threadState;
	}

	public State getThreadState() {

		// get current thread state
		State threadState = threadHelper.getThreadState(threadId);
		if (threadState != null) {
			// return current thread state
            return threadState;
		}
        else {
            // return cached thread state
            return this.threadState;
        }
	}
	
	public void setThreadCpuUsage(double threadCpuUsage) {
		this.threadCpuUsage = threadCpuUsage;
	}

	public double getThreadCpuUsage() {
		return threadCpuUsage;
	}

	public String getStackTrace( int maxDepth ) {

		return threadHelper.getStackTrace( threadId, maxDepth );

	}

	public String getStackTraceWithContext(int maxDepth) {

		return threadHelper.getStackTraceWithContext(threadId, maxDepth);

	}

	public StackTraceElement[] getStackTraceElements( int maxDepth ) {

		return threadHelper.getStackTraceElements(threadId, maxDepth);

	}

	public StackTraceElement[] getStackTraceElements( ) {

		return threadHelper.getStackTraceElements(threadId);

	}

    public ThreadInfo getThreadInfo() {
        return threadInfo;
    }

    public void setThreadInfo(ThreadInfo threadInfo) {
        this.threadInfo = threadInfo;
    }
}
