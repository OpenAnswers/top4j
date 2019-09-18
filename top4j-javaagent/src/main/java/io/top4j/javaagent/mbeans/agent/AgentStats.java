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
import io.top4j.javaagent.profiler.CpuTime;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import java.util.logging.Logger;

public class AgentStats implements AgentStatsMXBean {

	volatile private double agentCpuTime;
	volatile private double agentCpuUtil;
	volatile private double mBeanCpuTime;
	volatile private long iterations;
	private int availableProcessors;
	private long lastCpuTime;
	private long lastSystemTime;
	private CpuTime cpuTime = new CpuTime();
	private List<StatsMXBean> jvmStatsMBeans;
	private boolean enabled = true;
	private String failureReason;

	private static final Logger LOGGER = Logger.getLogger(AgentStats.class.getName());
	
	public AgentStats ( List<StatsMXBean> jvmStatsMBeans ) {
		
		this.setAgentCpuUtil(0);
		this.setMBeanCpuTime(0);
		this.setIterations(0);
		this.setLastCpuTime(0);
		long systemTime = System.currentTimeMillis();
		this.setLastSystemTime( systemTime );
        this.jvmStatsMBeans = jvmStatsMBeans;
		
		final OperatingSystemMXBean osbean =
	            ManagementFactory.getOperatingSystemMXBean();
		this.setAvailableProcessors(osbean.getAvailableProcessors());

	}

	@Override
	public void setAgentCpuTime(double cpuTime) {
		this.agentCpuTime = cpuTime;
	}

	@Override
	public double getAgentCpuTime() {
		return agentCpuTime;
	}

	@Override
	public void setAgentCpuUtil(double agentCpuUtil) {
		this.agentCpuUtil = agentCpuUtil;

	}

	@Override
	public double getAgentCpuUtil() {
		return agentCpuUtil;
	}

	@Override
	public void setIterations(long iterations) {
		this.iterations = iterations;
	}

	@Override
	public long getIterations() {
		return iterations;
	}

	@Override
	/* Update Agent Stats */
	public synchronized void update() {

		if (enabled) {
			try {
				// update Agent stats
				updateAgentStats();
			} catch (Exception e) {
				// something went wrong - record failure reason and disable any further updates
				this.failureReason = e.getMessage();
				this.enabled = false;
				LOGGER.severe("TOP4J ERROR: Failed to update AgentStats MBean due to: " + e.getMessage());
				LOGGER.severe("TOP4J ERROR: Further AgentStats MBean updates will be disabled from now on.");
			}
		}

	}

	private synchronized void updateAgentStats( ) {

		// initialise thread CPU timer
		cpuTime.init();

		LOGGER.fine("Updating Agent stats....");
		
		// update CPU util
		updateCpuUtil();
		
		// update iterations
		updateIterations();

		// update agent stats CPU time
		mBeanCpuTime = cpuTime.getMillis();

	}
	
	private void updateCpuUtil() {

		// get current system time
		long systemTime = System.currentTimeMillis();
		// calculate time difference since last update
		long timeDiffMillis = systemTime - lastSystemTime;
        // aggregate agentCpuTime in milliseconds accumulated during this iteration
        double agentCpuTime = 0;
        for (StatsMXBean jvmStats : jvmStatsMBeans) {
            agentCpuTime+=jvmStats.getMBeanCpuTime();
        }
        double threadCpuUsage = ( agentCpuTime / timeDiffMillis) * 100;
        double cpuUtil = threadCpuUsage / availableProcessors;
        LOGGER.fine("Agent CPU Util = " + cpuUtil + "%");
        LOGGER.fine("Agent CPU Time = " + agentCpuTime + "ms");
		// update agent CPU util
		this.agentCpuUtil = cpuUtil;
		// update agent CPU time
		this.agentCpuTime = agentCpuTime;
		// update last system time
		this.lastSystemTime = systemTime;
	}
	
	private synchronized void updateIterations() {
		
		// update iterations
		this.iterations++;
	}

	public long getLastCpuTime() {
		return lastCpuTime;
	}

	public void setLastCpuTime(long lastCpuTime) {
		this.lastCpuTime = lastCpuTime;
	}

	public int getAvailableProcessors() {
		return availableProcessors;
	}

	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	public long getLastSystemTime() {
		return lastSystemTime;
	}

	public void setLastSystemTime(long lastSystemTime) {
		this.lastSystemTime = lastSystemTime;
	}

	@Override
	public void setMBeanCpuTime(double agentCpuTime) {
		this.mBeanCpuTime = agentCpuTime;
		
	}

	@Override
	public double getMBeanCpuTime() {
		return mBeanCpuTime;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean getEnabled() {
		return this.enabled;
	}

	@Override
	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	@Override
	public String getFailureReason() {
		return this.failureReason;
	}

}
