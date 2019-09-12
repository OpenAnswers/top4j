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

import io.top4j.javaagent.config.Configurator;
import io.top4j.javaagent.profiler.CpuTime;

import java.io.IOException;
import java.util.Map;
import java.util.logging.*;

public class ThreadStats implements ThreadStatsMXBean {
	
	private ThreadUsage threadUsage;
    private CpuTime cpuTime = new CpuTime();
	private volatile double mBeanCpuTime;
    private boolean hotMethodProfilingEnabled;

	private static final Logger LOGGER = Logger.getLogger(ThreadStats.class.getName());
	
	public ThreadStats (Configurator config, Map<Integer, TopThread> topThreadsMap ) throws IOException {
		
		LOGGER.fine("Initialising Thread Stats....");

        this.hotMethodProfilingEnabled = false;
		// instantiate new ThreadUsage object used to track thread usage
		this.threadUsage = new ThreadUsage( config, topThreadsMap );
		
	}

	public ThreadStats ( Configurator config, Map<Integer, TopThread> topThreadsMap, Map<Integer, BlockedThread> blockedThreadsMap ) throws IOException {

		LOGGER.fine("Initialising Thread Stats with thread contention monitoring enabled....");

        this.hotMethodProfilingEnabled = false;
		// instantiate new ThreadUsage object used to track thread usage
		this.threadUsage = new ThreadUsage( config, topThreadsMap, blockedThreadsMap );

	}

	public  ThreadStats ( Configurator config, Map<Integer, TopThread> topThreadsMap, HotMethods hotMethods, long hotMethodPollInterval ) throws IOException {

		LOGGER.fine("Initialising Thread Stats with hot method profiling enabled....");

        this.hotMethodProfilingEnabled = true;
		// instantiate new ThreadUsage object used to track thread usage
		this.threadUsage = new ThreadUsage( config, topThreadsMap, hotMethods, hotMethodPollInterval );

	}

	public ThreadStats ( Configurator config, Map<Integer, TopThread> topThreadsMap, Map<Integer, BlockedThread> blockedThreadsMap, HotMethods hotMethods, long hotMethodPollInterval ) throws IOException {

		LOGGER.fine("Initialising Thread Stats with thread contention monitoring and hot method profiling enabled....");

        this.hotMethodProfilingEnabled = true;
		// instantiate new ThreadUsage object used to track thread usage
		this.threadUsage = new ThreadUsage( config, topThreadsMap, blockedThreadsMap, hotMethods, hotMethodPollInterval );

	}


	/** Update Thread stats. */
    public synchronized void update( ) {

        // initialise thread CPU timer
    	cpuTime.init();

    	LOGGER.fine("Updating Thread Stats....");

    	// update CPU usage
    	threadUsage.update();

        // update thread stats CPU time
        if (hotMethodProfilingEnabled) {
            mBeanCpuTime = cpuTime.getMillis() + threadUsage.getHotMethods().getAndResetMBeanCpuTime();
        } else {
            mBeanCpuTime = cpuTime.getMillis();
        }

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
	public void setCpuUsage(double threadUsage) {
		this.threadUsage.setCpuUsage(threadUsage);
	}

	@Override
	public double getCpuUsage() {
		return this.threadUsage.getCpuUsage();
	}

	@Override
	public void setUserCpuUsage(double userCpuUsage) {
		this.threadUsage.setUserCpuUsage(userCpuUsage);
	}

	@Override
	public double getUserCpuUsage() {
		return this.threadUsage.getUserCpuUsage();
	}

	@Override
	public void setSysCpuUsage(double sysCpuUsage) {
		this.threadUsage.setSysCpuUsage(sysCpuUsage);
	}

	@Override
	public double getSysCpuUsage() {
		return this.threadUsage.getSysCpuUsage();
	}

	@Override
	public long getThreadCount() {
		return this.threadUsage.getThreadCount();
	}

	@Override
	public void setThreadCount(long threadCount) {
		this.threadUsage.setThreadCount(threadCount);
	}

	@Override
	public long getRunnableThreadCount() {
		return this.threadUsage.getRunnableThreadCount();
	}

	@Override
	public void setRunnableThreadCount(long runnableThreadCount) {
		this.threadUsage.setRunnableThreadCount(runnableThreadCount);
	}

	@Override
	public long getBlockedThreadCount() {
		return this.threadUsage.getBlockedThreadCount();
	}

	@Override
	public void setBlockedThreadCount(long blockedThreadCount) {
		this.threadUsage.setBlockedThreadCount(blockedThreadCount);
	}

	@Override
	public long getWaitingThreadCount() {
		return this.threadUsage.getWaitingThreadCount();
	}

	@Override
	public void setWaitingThreadCount(long waitingThreadCount) {
		this.threadUsage.setWaitingThreadCount(waitingThreadCount);
	}

	@Override
	public long getTimedWaitingThreadCount() {
		return this.threadUsage.getTimedWaitingThreadCount();
	}

	@Override
	public void setTimedWaitingThreadCount(long timedWaitingThreadCount) {
		this.threadUsage.setTimedWaitingThreadCount(timedWaitingThreadCount);
	}

}
