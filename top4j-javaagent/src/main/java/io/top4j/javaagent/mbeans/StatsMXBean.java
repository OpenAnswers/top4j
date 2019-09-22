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

package io.top4j.javaagent.mbeans;

/**
 * Base stats MBean used to define MBean attributes and operations common to all Top4J MBeans.
 */
public interface StatsMXBean {

    /**
     ** Update MBean attributes with latest data.
     */
    void update();

    /**
     * Sets the amount of CPU time in milliseconds consumed by the MBean update() operation per invocation.
     * @param mBeanCpuTime the amount of CPU time in milliseconds
     */
    void setMBeanCpuTime(double mBeanCpuTime);

    /**
     * Returns the amount of CPU time in milliseconds consumed by the MBean update() operation per invocation.
     * @return the amount of CPU time in milliseconds
     */
    double getMBeanCpuTime();

    /**
     * Sets whether the MBean is enabled (true) or disabled (false).
     * <p>
     * Disabled MBeans will no longer receive updates via the MBean update() operation or the Top4J Java Agent stats update process.
     * An MBean will be marked as disabled if the update() operation encounters an unexpected exception.
     * @param enabled whether the MBean is enabled (true) or disabled (false).
     */
    void setEnabled(boolean enabled);

    /**
     * Returns whether the MBean is enabled (true) or disabled (false).
     * <p>
     * Disabled MBeans will no longer receive updates via the MBean update() operation or the Top4J Java Agent stats update process.
     * @return whether the MBean is enabled (true) or disabled (false)
     */
    boolean getEnabled();

    /**
     * Sets the MBean failure reason.
     * If the update() operation encounters an unexpected exception the failure reason will be stored within the FailureReason MBean attribute.
     * @param failureReason the failure reason
     */
    void setFailureReason(String failureReason);

    /**
     * Returns the MBean failure reason.
     * If the update() operation encounters an unexpected exception the failure reason will be stored within the FailureReason MBean attribute.
     * @return the failure reason
     */
    String getFailureReason();

}
