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
 * Used to store and expose stats relating to a thread that has been determined as a top CPU consumer during the last iteration.
 * Each top thread is ranked between 1 and N via the "rank" attribute on the TopThread MBean Object Name, where 1 is the thread
 * that has consumed the most CPU and N is the thread that has consumed the least (out of the top ranked threads).
 */

public interface TopThreadMXBean {

    /**
     * Sets the name of the top thread.
     * @param threadName the thread name
     */
    void setThreadName(String threadName);

    /**
     * Returns the name of the top thread.
     * @return the thread name
     */
    String getThreadName();

    /**
     * Sets the thread ID of the top thread.
     * @param threadId the thread ID
     */
    void setThreadId(long threadId);

    /**
     * Returns the thread ID of the top thread.
     * @return the thread ID
     */
    long getThreadId();

    /**
     * Sets the thread state as defined by the <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html">java.lang.Thread.State Enum</a>.
     * @param threadState the thread state
     */
    void setThreadState(Thread.State threadState);

    /**
     * Returns the thread state as defined by the <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.State.html">java.lang.Thread.State Enum</a>.
     * @return the thread state
     */
    Thread.State getThreadState();

    /**
     * Sets the thread CPU time as a percentage of the total CPU time available during the last iteration.
     * <p>
     * The thread CPU usage is calculated as the total CPU time consumed by a thread divided by the total CPU time available (wall clock time) multiplied by 100.
     * In other words.... ( threadCpuTime / elapsedTime ) * 100
     * @param threadCpuUsage the thread CPU usage
     */
    void setThreadCpuUsage(double threadCpuUsage);

    /**
     * Returns the thread CPU time as a percentage of the total CPU time available during the last iteration.
     * <p>
     * The thread CPU usage is calculated as the total CPU time consumed by a thread divided by the total CPU time available (wall clock time) multiplied by 100.
     * In other words.... ( threadCpuTime / elapsedTime ) * 100
     * @return the thread CPU usage
     */
    double getThreadCpuUsage();

    /**
     * Returns the stack trace for this top thread with a maximum frame depth of maxDepth.
     * @param maxDepth maximum frame depth
     * @return a String representation of the stack trace
     */
    String getStackTrace(int maxDepth);

    /**
     * Returns the stack trace for this blocked thread with context, e.g. thread name and thread state, with a maximum frame depth of maxDepth.
     * @param maxDepth maximum frame depth
     * @return a String representation of the stack trace with some additional context
     */
    String getStackTraceWithContext(int maxDepth);

}
