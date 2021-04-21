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

import io.top4j.javaagent.mbeans.StatsMXBean;

/**
 * Used to store and expose stats relating to JVM thread usage.
 */

public interface ThreadStatsMXBean extends StatsMXBean {

    /**
     * Sets the combined CPU usage of all JVM threads as a percentage of the total CPU available during the last iteration.
     * <p>
     * The CPU usage is calculated as the total CPU usage consumed by all JVM threads divided by the number of processor cores available
     * to give the system level CPU usage for the JVM process.
     * @param cpuUsage the CPU usage
     */
    void setCpuUsage(double cpuUsage);

    /**
     * Returns the combined CPU usage of all JVM threads as a percentage of the total CPU available during the last iteration.
     * <p>
     * The CPU usage is calculated as the total CPU usage consumed by all JVM threads divided by the number of processor cores available
     * to give the system level CPU usage for the JVM process.
     * @return the CPU usage
     */
    double getCpuUsage();

    /**
     * Sets the combined user space CPU usage of all JVM threads as a percentage of the total CPU available during the last iteration.
     * <p>
     * The user CPU usage is calculated as the total user CPU usage consumed by all JVM threads divided by the number of processor cores available
     * to give the system level user CPU usage for the JVM process.
     * @param userCpuUsage the user CPU usage
     */
    void setUserCpuUsage(double userCpuUsage);

    /**
     * Returns the combined user space CPU usage of all JVM threads as a percentage of the total CPU available during the last iteration.
     * <p>
     * The user CPU usage is calculated as the total user CPU usage consumed by all JVM threads divided by the number of processor cores available
     * to give the system level user CPU usage for the JVM process.
     * @return the user CPU usage
     */
    double getUserCpuUsage();

    /**
     * Sets the combined system CPU usage of all JVM threads as a percentage of the total CPU available during the last iteration.
     * <p>
     * The system CPU usage is calculated as the total CPU usage consumed by all JVM threads minus the total user CPU usage.
     * @param sysCpuUsage the system CPU usage
     */
    void setSysCpuUsage(double sysCpuUsage);

    /**
     * Returns the combined system CPU usage of all JVM threads as a percentage of the total CPU available during the last iteration.
     * <p>
     * The system CPU usage is calculated as the total CPU usage consumed by all JVM threads minus the total user CPU usage.
     * @return the system CPU usage
     */
    double getSysCpuUsage();

    /**
     * Get Process CPU usage (&lt;0 if not available).
     * @return the Java process CPU usage
     */
    public double getProcessCpuUsage();

    /**
     * Sets the total number of threads running within the JVM process at the time of the last stats update.
     * @param threadCount the thread count
     */
    void setThreadCount(long threadCount);

    /**
     * Returns the total number of threads running within the JVM process at the time of the last stats update.
     * @return the thread count
     */
    long getThreadCount();

    /**
     * Sets the total number of runnable threads within the JVM process at the time of the last stats update.
     * In other words, the total number of threads in a RUNNABLE state.
     * @param runnableThreadCount the RUNNABLE thread count
     */
    void setRunnableThreadCount(long runnableThreadCount);

    /**
     * Returns the total number of runnable threads within the JVM process at the time of the last stats update.
     * In other words, the total number of threads in a RUNNABLE state.
     * @return the RUNNABLE thread count
     */
    long getRunnableThreadCount();

    /**
     * Sets the total number of blocked threads within the JVM process at the time of the last stats update.
     * In other words, the total number of threads in a BLOCKED state.
     * @param blockedThreadCount the BLOCKED thread count
     */
    void setBlockedThreadCount(long blockedThreadCount);

    /**
     * Returns the total number of blocked threads within the JVM process at the time of the last stats update.
     * In other words, the total number of threads in a BLOCKED state.
     * @return the BLOCKED thread count
     */
    long getBlockedThreadCount();

    /**
     * Sets the total number of waiting threads within the JVM process at the time of the last stats update.
     * In other words, the total number of threads in a WAITING state.
     * @param waitingThreadCount the WAITING thread count
     */
    void setWaitingThreadCount(long waitingThreadCount);

    /**
     * Returns the total number of waiting threads within the JVM process at the time of the last stats update.
     * In other words, the total number of threads in a WAITING state.
     * @return the WAITING thread count
     */
    long getWaitingThreadCount();

    /**
     * Sets the total number of timed waiting threads within the JVM process at the time of the last stats update.
     * In other words, the total number of threads in a TIMED_WAITING state.
     * @param timedWaitingThreadCount the TIME_WAITING thread count
     */
    void setTimedWaitingThreadCount(long timedWaitingThreadCount);

    /**
     * Returns the total number of timed waiting threads within the JVM process at the time of the last stats update.
     * In other words, the total number of threads in a TIMED_WAITING state.
     * @return the TIME_WAITING thread count
     */
    long getTimedWaitingThreadCount();

}
