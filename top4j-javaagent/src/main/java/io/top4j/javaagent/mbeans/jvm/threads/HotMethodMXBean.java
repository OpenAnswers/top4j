package io.top4j.javaagent.mbeans.jvm.threads;

/**
 * Created by ryan on 27/07/15.
 */
public interface HotMethodMXBean {

	void setMethodName( String methodName );

	String getMethodName();

    void setThreadName( String threadName );

	String getThreadName();

	void setThreadId( long threadId );

	long getThreadId();

	void setLoadProfile( double loadProfile );

	double getLoadProfile();

	String getStackTrace( int maxDepth );

}
