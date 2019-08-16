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

package io.top4j.javaagent.controller;

import java.lang.management.ManagementFactory;
import java.util.TimerTask;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import io.top4j.javaagent.config.Configurator;
import io.top4j.javaagent.config.Constants;
import io.top4j.javaagent.mbeans.jvm.JVMStatsMXBean;

public class Collector extends TimerTask {
	
	private JVMStatsMXBean jvmStats;
	private boolean statsLoggerEnabled;

	public Collector ( Configurator config ) throws Exception {
		
        this.statsLoggerEnabled = config.isStatsLoggerEnabled();
        
     	// create JVMStats objectName
		try {
			ObjectName jvmStatsObjectName = new ObjectName(Constants.DOMAIN + ":type=" + Constants.AGENT_TYPE + ",statsType=" + Constants.JVM_STATS_TYPE);

			// instantiate new JVMStatsMXBean proxy based on jvmStatsObjectName
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

			this.jvmStats = JMX.newMBeanProxy(mbs, jvmStatsObjectName, JVMStatsMXBean.class);

		} catch (MalformedObjectNameException e) {
			throw new Exception( "JMX MalformedObjectNameException: " + e.getMessage() );
		}
		
	}
	
	@Override
	public void run() {

		// update stats
    	updateStats( );
    	if (statsLoggerEnabled) {
    		// log stats
    		logStats( );
    	}
		
	}
	
	private void logStats( ) {
    	
    	// log JVM stats
		jvmStats.log();
		
	}

    public void updateStats( ) {
    	
    	// update JVM stats
    	jvmStats.update();
        
    }
    
}
