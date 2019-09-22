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

package io.top4j.javaagent.mbeans.agent;

import io.top4j.javaagent.mbeans.StatsMXBean;

/**
 * Used to store and expose stats relating to the Top4J JavaAgent run-time.
 */

public interface AgentStatsMXBean extends StatsMXBean {

    /**
     * Sets the amount of CPU time in milliseconds consumed by all MBean update() operations per invocation.
     * @param cpuTime CPU time in milliseconds
     */
    void setAgentCpuTime(double cpuTime);

    /**
     * Returns the amount of CPU time in milliseconds consumed by all MBean update() operations per invocation.
     * @return the CPU time in milliseconds
     */
    double getAgentCpuTime();

    /**
     * Sets the percentage CPU utilisation of the Top4J JavaAgent background threads.
     * @param cpuUtil CPU utilisation as a percentage of wall clock time
     */
    void setAgentCpuUtil(double cpuUtil);

    /**
     * Returns the percentage CPU utilisation of the Top4J JavaAgent background threads.
     * @return CPU utilisation as a percentage of wall clock time
     */
    double getAgentCpuUtil();

    /**
     * Sets the number of Top4J JavaAgent stats update iterations since the JavaAgent was enabled.
     * @param iterations the number of iterations
     */
    void setIterations(long iterations);

    /**
     * Returns the number of Top4J JavaAgent stats update iterations since the JavaAgent was enabled.
     * @return the number of iterations
     */
    long getIterations();

}
