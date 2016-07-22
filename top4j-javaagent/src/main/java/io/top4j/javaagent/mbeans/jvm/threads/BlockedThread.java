package io.top4j.javaagent.mbeans.jvm.threads;

import io.top4j.javaagent.utils.ThreadHelper;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.Thread.State;


public class BlockedThread implements BlockedThreadMXBean {
	
	volatile private String threadName;
	volatile private long threadId;
	volatile private State threadState;
	volatile private long threadBlockedTime;
	volatile private double threadBlockedPercentage;
	private ThreadHelper threadHelper;

	public BlockedThread( MBeanServerConnection mbsc ) throws IOException {

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
		return threadState;
	}

	@Override
	public void setThreadBlockedTime(long threadBlockedTime) {

		this.threadBlockedTime = threadBlockedTime;

	}

	@Override
	public long getThreadBlockedTime() {
		return threadBlockedTime;
	}

	@Override
	public void setThreadBlockedPercentage(double threadBlockedPercentage) {

		this.threadBlockedPercentage = threadBlockedPercentage;

	}

	@Override
	public double getThreadBlockedPercentage() {
		return threadBlockedPercentage;
	}

	public String getStackTrace( int maxDepth ) {

		return threadHelper.getStackTrace( threadId, maxDepth );

	}

	public String getStackTraceWithContext(int maxDepth) {

		return threadHelper.getStackTraceWithContext( threadId, maxDepth );

	}
	
}