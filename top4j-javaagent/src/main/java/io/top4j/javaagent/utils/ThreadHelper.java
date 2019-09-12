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

package io.top4j.javaagent.utils;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.Thread.State;

/**
 * Created by ryan on 17/05/15.
 */
public class ThreadHelper {

    ThreadMXBean threadMXBean;

	public ThreadHelper( MBeanServerConnection mbsc ) throws IOException {

		this.threadMXBean = ManagementFactory.getPlatformMXBean( mbsc, ThreadMXBean.class);

	}

    public StackTraceElement[] getStackTraceElements( ThreadInfo threadInfo ) {

        StackTraceElement[] stackTraceElements;
        if (threadInfo != null) {
            // get stack trace
            stackTraceElements = threadInfo.getStackTrace();
        } else {
            // assume thread has terminated
            stackTraceElements = null;
        }

        return stackTraceElements;
    }

	public StackTraceElement[] getStackTraceElements( long threadId, int maxDepth ) {

		if (threadId == 0) {
			return new StackTraceElement[0];
		}
		if (maxDepth == 0) {
			maxDepth = Integer.MAX_VALUE;
		}

		ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId, maxDepth);

		return getStackTraceElements( threadInfo );
	}

    public StackTraceElement[] getStackTraceElements( long threadId ) {

        return getStackTraceElements( threadId, 0 );

    }
    public String getStackTrace( long threadId, int maxDepth ) {

        // get stack trace
		StackTraceElement[] stackTraceElements = getStackTraceElements(threadId, maxDepth);

		if (stackTraceElements != null) {

			return getStackTraceAsString(stackTraceElements);

		} else {

			return "No stack trace available.";
		}
	}

	public String getStackTraceWithContext( long threadId, int maxDepth ) {

		if (threadId == 0) {
			return null;
		}
		if (maxDepth == 0) {
			maxDepth = Integer.MAX_VALUE;
		}
		ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId, maxDepth);
		String threadName;
		String threadState;
		StackTraceElement[] stackTraceElements;
		if (threadInfo != null) {
			// get threadName
			threadName = threadInfo.getThreadName();
			// get threadState
			threadState = threadInfo.getThreadState().toString();
			// get stack trace
			stackTraceElements = threadInfo.getStackTrace();
		} else {
			// assume thread has terminated
			threadName = "TERMINATED";
			threadState = Thread.State.TERMINATED.toString();
			stackTraceElements = null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Name: ").append(threadName).append("\n");
		sb.append("State: ").append(threadState).append("\n\n");

		if (stackTraceElements != null) {

			sb.append( getStackTraceAsString(stackTraceElements) );
		} else {

			sb.append("No stack trace available.");
		}

		return sb.toString();

	}

	public String getStackTraceAsString( StackTraceElement[] stackTraceElements ) {

		StringBuilder sb = new StringBuilder();
		sb.append("Stack trace:");
		for (StackTraceElement element : stackTraceElements ) {
			sb.append("\n");
			sb.append(element.toString());
		}
		return sb.toString();
	}

    public State getThreadState( long threadId ) {

        if (threadId == 0) {
            return null;
        }

        ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId);

        if (threadInfo != null) {
            // get thread state
            return threadInfo.getThreadState();
        } else {
            // assume thread has terminated
            return null;
        }

    }

    public long getThreadCpuTime( long threadId ) {

        if (threadId == 0) {
            return -1;
        }

        return threadMXBean.getThreadCpuTime( threadId );

    }
}
