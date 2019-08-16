/*
 * Copyright (c) 2019 Open Answers Ltd.
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
