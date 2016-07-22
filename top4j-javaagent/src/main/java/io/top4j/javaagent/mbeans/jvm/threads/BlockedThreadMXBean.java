package io.top4j.javaagent.mbeans.jvm.threads;

/**
 * Created by ryan on 17/05/15.
 */
public interface BlockedThreadMXBean {

    public void setThreadName( String threadName );

	public String getThreadName();

	public void setThreadId( long threadId );

	public long getThreadId();

	public void setThreadState( Thread.State threadState );

	public Thread.State getThreadState();

	public void setThreadBlockedTime( long threadBlockedTime );

	public long getThreadBlockedTime();

	public void setThreadBlockedPercentage( double threadBlockedPercentage );

	public double getThreadBlockedPercentage();

	public String getStackTrace( int maxDepth );

	public String getStackTraceWithContext( int maxDepth );

}
