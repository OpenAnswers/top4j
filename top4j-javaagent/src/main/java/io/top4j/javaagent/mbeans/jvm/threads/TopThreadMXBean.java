package io.top4j.javaagent.mbeans.jvm.threads;

public interface TopThreadMXBean {
	
	void setThreadName( String threadName );
	
	String getThreadName();
	
	void setThreadId( long threadId );
	
	long getThreadId();
	
	void setThreadState( Thread.State threadState );
	
	Thread.State getThreadState();
	
	void setThreadCpuUsage( double threadCpuUsage );
	
	double getThreadCpuUsage();
	
	String getStackTrace( int maxDepth );
	
	String getStackTraceWithContext( int maxDepth );
	
}
