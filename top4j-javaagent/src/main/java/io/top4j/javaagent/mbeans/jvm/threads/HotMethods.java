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

import io.top4j.javaagent.utils.ThreadHelper;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class HotMethods {
	
	//private Map<String, Integer> invocationCounter;
	private Map<String, StackTraceElement[]> stackTraces;
	private Map<String, String> threadNames;
	private Map<String, Long> threadIds;
    private Map<String, Long> hotMethodCpuTime;
    private Map<Long, Long> threadCpuTime;
    private long totalCpuTime;
	private Map<Integer, HotMethod> hotMethods;
	private int hotMethodCount;
    private final Object lock = new Object();
    private ThreadHelper threadHelper;
    private double mBeanCpuTime;

    private static final Logger LOGGER = Logger.getLogger(HotMethods.class.getName());

	public HotMethods ( MBeanServerConnection mbsc, Map<Integer, HotMethod> hotMethods ) throws IOException {
		
		//this.invocationCounter = new HashMap<>();
		this.stackTraces = new HashMap<>();
		this.threadNames = new HashMap<>();
		this.threadIds = new HashMap<>();
        this.hotMethodCpuTime = new HashMap<>();
        this.threadCpuTime = new HashMap<>();
		this.hotMethods = hotMethods;
		this.hotMethodCount = hotMethods.size();
        this.threadHelper = new ThreadHelper( mbsc );
	}
	
	public void addHotMethod ( StackTraceElement[] ste, String threadName, long threadId ) {

        synchronized (lock) {

            String hotMethod;
            if (ste.length > 0 && threadName != null && threadId != 0) {
                // get hot method
                hotMethod = ste[0].getClassName() + "." + ste[0].getMethodName();
                LOGGER.finer("Adding hot method " + hotMethod + " to hot methods store.");
            }
            else {
                return;
            }

            // store hot method stack trace
            stackTraces.put(hotMethod, ste);

            // store hot method thread name
            this.threadNames.put(hotMethod, threadName);

            // store hot method thread Id
            this.threadIds.put(hotMethod, threadId);

            // get thread CPU time
            long cpuTime = threadHelper.getThreadCpuTime( threadId );

            if (cpuTime > -1) {

                // add cpuTime to totalCpuTime
                totalCpuTime+=cpuTime;

                if ( threadCpuTime.containsKey( threadId ) ) {
                    // get previous thread CPU time
                    long previousCpuTime = threadCpuTime.get( threadId );
                    // calculate CPU burn since last iteration
                    long cpuTimeDiff = cpuTime - previousCpuTime;
                    // store new thread CPU time
                    threadCpuTime.put( threadId, cpuTime );

                    if ( hotMethodCpuTime.containsKey( hotMethod ) ) {
                        // get previous hot method CPU time
                        long previousHotMethodCpuTime = hotMethodCpuTime.get( hotMethod );
                        // store new hot method CPU time
                        hotMethodCpuTime.put( hotMethod, previousHotMethodCpuTime + cpuTimeDiff );
                        //LOGGER.finer("Hot Method: " + hotMethod + ", CPU Time: " + (double) cpuTimeDiff / 1000000 + "ms");
                    }
                    else {
                        // initialise hot method CPU time
                        hotMethodCpuTime.put( hotMethod, cpuTimeDiff );
                    }
                }
                else {
                    // initialise thread CPU time
                    threadCpuTime.put( threadId, cpuTime );
                }
            }

        }
	}

	public Map<Integer, HotMethod> get( ) {

        synchronized (lock) {
            return hotMethods;
        }
	}

	public void update( ) {

        synchronized (lock) {

            // convert invocationCounter to TreeMap
            MethodTimeMap hotMethodSortedMap = new MethodTimeMap();
            for (Map.Entry<String, Long> entry : hotMethodCpuTime.entrySet()) {
                hotMethodSortedMap.put(entry.getValue(), entry.getKey());
            }
            LOGGER.finer("Hot Method Sorted Map: " + hotMethodSortedMap.toString());
            if ( hotMethodSortedMap.size() == 0 )  {
                // no hot methods to process on this occasion - set default values and return
                setDefaultValues();
                return;
            }

            // convert hotMethodSortedMap to object array
            Object[] hotMethodArray = hotMethodSortedMap.descendingKeySet().toArray();

            // get hotMethodSortedMap size
            int hotMethodMapSize = hotMethodSortedMap.size();

            // initialise hotMethodCounter
            int hotMethodCounter = 1;

            hotMethodCountLoop:
            for (int i = 0; i < hotMethodCount; i++) {

                if (hotMethodCounter >= hotMethodMapSize) {
                    // no more hot methods to process - set default values and continue
                    // retrieve hotMethodCounter HotMethod
                    HotMethod hotMethodMBean = hotMethods.get(hotMethodCounter);
                    setDefaultValues(hotMethodMBean);
                    // check hotMethodCount
                    if (hotMethodCounter == hotMethodCount) {
                        // hotMethodCount reached - break out of loop
                        break hotMethodCountLoop;
                    }
                    // increment hotMethodCounter and continue
                    hotMethodCounter++;
                    continue;
                }

                // get hotMethodCpuTime
                Long hotMethodCpuTime = (Long) hotMethodArray[i];

                // get methodNames
                List<String> methodNames = hotMethodSortedMap.get( hotMethodCpuTime );

                for (String methodName : methodNames) {

                    // get threadName
                    String threadName = threadNames.get(methodName);
                    // get threadId
                    Long threadId = threadIds.get(methodName);
                    // get stackTrace
                    StackTraceElement[] stackTrace = stackTraces.get(methodName);
                    // calculate load profile
                    double loadProfile = ( (double) hotMethodCpuTime / totalCpuTime ) * 100;
                    // retrieve hotMethodCounter HotMethod
                    HotMethod hotMethodMBean = hotMethods.get(hotMethodCounter);
                    // update hotMethodsMBean attributes
                    hotMethodMBean.setMethodName(methodName);
                    hotMethodMBean.setThreadName(threadName);
                    hotMethodMBean.setThreadId(threadId);
                    hotMethodMBean.setStackTrace(stackTrace);
                    hotMethodMBean.setLoadProfile(loadProfile);
                    LOGGER.finer("hotMethodCounter: " + hotMethodCounter + ", methodName: " + methodName +
                            ", threadName: " + threadName + ", loadProfile: " + loadProfile);
                    // check hotMethodCount
                    if (hotMethodCounter == hotMethodCount) {
                        // hotMethodCount reached - break out of loop
                        break hotMethodCountLoop;
                    }
                    // increment hotMethodCounter
                    hotMethodCounter++;
                }

            }

            // clear down hot method maps
            //invocationCounter.clear();
            stackTraces.clear();
            threadNames.clear();
            threadIds.clear();
            hotMethodCpuTime.clear();
            threadCpuTime.clear();
            totalCpuTime=0;
        }

	}

    private void setDefaultValues( HotMethod hotMethod ) {

        // set default values on hotMethod MBean attributes
        hotMethod.setMethodName(null);
        hotMethod.setStackTrace(null);
        hotMethod.setThreadId(0);
        hotMethod.setThreadName( null );

    }

    private void setDefaultValues() {

        // set default values on all hotMethod MBean attributes
        for (Map.Entry<Integer, HotMethod> entry : hotMethods.entrySet()) {
            setDefaultValues( entry.getValue() );
        }
    }

    public void addMBeanCpuTime(double agentCpuTime) {

        synchronized (lock) {

            // add mBeanCpuTime
            this.mBeanCpuTime +=agentCpuTime;
        }

    }

    public double getAndResetMBeanCpuTime() {

        synchronized (lock) {

            double agentCpuTime = this.mBeanCpuTime;
            this.mBeanCpuTime = 0;
            LOGGER.finer("Hot Method Tracker Accumulative CPU Time: " + agentCpuTime);
            return agentCpuTime;
        }

    }

}
