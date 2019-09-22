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

package io.top4j.javaagent.mbeans.jvm.threads;

/**
 * Used to store and expose stats relating to a thread that has been blocked for some time during the last iteration.
 * Each blocked thread is ranked between 1 and N via the "rank" attribute on the BlockedThread MBean Object Name,
 * where 1 is the thread that has been blocked the most and N is the thread that has been blocked the least
 * (out of the top ranked blocked threads).
 */

public interface BlockedThreadMXBean {

    /**
     * Sets the name of the blocked thread.
     * @param threadName the thread name
     */
    public void setThreadName(String threadName);

    /**
     * Returns the name of the blocked thread.
     * @return the thread name
     */
    public String getThreadName();

    /**
     * Sets the thread ID of the blocked thread.
     * @param threadId the thread ID
     */
    public void setThreadId(long threadId);

    /**
     * Returns the thread ID of the blocked thread.
     * @return the thread ID
     */
    public long getThreadId();

    /**
     * Sets the thread state as defined by the <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html">java.lang.Thread.State Enum</a>.
     * @param threadState the thread state
     */
    public void setThreadState(Thread.State threadState);

    /**
     * Sets the thread state as defined by the <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html">java.lang.Thread.State Enum</a>.
     * @return the thread state
     */
    public Thread.State getThreadState();

    /**
     * Sets the time in milliseconds that the thread has been in a blocked state during the last iteration.
     * @param threadBlockedTime blocked time in milliseconds
     */
    public void setThreadBlockedTime(long threadBlockedTime);

    /**
     * Returns the time in milliseconds that the thread has been in a blocked state during the last iteration.
     * @return blocked time in milliseconds
     */
    public long getThreadBlockedTime();

    /**
     * Sets the percentage of time that the thread has been in a blocked state during the last iteration.
     * <p>
     * A high thread blocked percentage can be an indicator of thread lock contention.
     * For example, threads blocked waiting to access a synchronised method or code block.
     * @param threadBlockedPercentage the thread blocked percentage
     */
    public void setThreadBlockedPercentage(double threadBlockedPercentage);

    /**
     * Returns the percentage of time that the thread has been in a blocked state during the last iteration.
     * <p>
     * A high thread blocked percentage can be an indicator of thread lock contention.
     * For example, threads blocked waiting to access a synchronised method or code block.
     * @return the thread blocked percentage
     */
    public double getThreadBlockedPercentage();

    /**
     * Returns the stack trace for this blocked thread with a maximum frame depth of maxDepth.
     * @param maxDepth maximum frame depth
     * @return a String representation of the stack trace
     */
    public String getStackTrace(int maxDepth);

    /**
     * Returns the stack trace for this blocked thread with context, e.g. thread name and thread state, with a maximum frame depth of maxDepth.
     * @param maxDepth maximum frame depth
     * @return a String representation of the stack trace with some additional context
     */
    public String getStackTraceWithContext(int maxDepth);

}
