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
 * Used to store and expose stats relating to a Java method that has been determined to have executed frequently.
 */

public interface HotMethodMXBean {

    /**
     * Sets the hot method name.
     * @param methodName the method name
     */
    void setMethodName(String methodName);

    /**
     * Returns the hot method name.
     * @return the method name
     */
    String getMethodName();

    /**
     * Sets the name of the thread that has executed the method most recently.
     * @param threadName the thread name
     */
    void setThreadName(String threadName);

    /**
     * Returns the name of the thread that has executed the method most recently.
     * @return the thread name
     */
    String getThreadName();

    /**
     * Sets the ID of the thread that has executed the method most recently.
     * @param threadId the thread ID
     */
    void setThreadId(long threadId);

    /**
     * Returns the ID of the thread that has executed the method most recently.
     * @return the thread ID
     */
    long getThreadId();

    /**
     * Sets the hot method load profile which is an indication of how hot the method has been during the last iteration.
     * <p>
     * The load profile is calculated as the CPU time used by a top thread that has executed a hot method as
     * a percentage of the total CPU time used by all top threads during the last iteration.
     * @param loadProfile the load profile
     */
    void setLoadProfile(double loadProfile);

    /**
     * Returns the hot method load profile which is an indication of how hot the method has been during the last iteration.
     * <p>
     * The load profile is calculated as the CPU time used by a top thread that has executed a hot method as
     * a percentage of the total CPU time used by all top threads during the last iteration.
     * @return the load profile
     */
    double getLoadProfile();

    /**
     * Returns the most recent stack trace for the hot method.
     * @param maxDepth
     * @return a String representation of the stack trace
     */
    String getStackTrace(int maxDepth);

}
