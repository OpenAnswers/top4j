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

package io.top4j.javaagent.messaging;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LoggerQueue {
	
	private LinkedBlockingQueue<String> loggerQueue;

	private static final Logger LOGGER = Logger.getLogger(LoggerQueue.class.getName());

	public LoggerQueue ( int capacity ) {

		this.loggerQueue = new LinkedBlockingQueue<>(capacity);
		
	}
	
	public boolean send( String notification ) {
		
		// send notification to loggerQueue
		return loggerQueue.offer(notification);
		
	}
	
	public boolean hasElements( ) {
		
		// test if the loggerQueue has elements
		if (loggerQueue.isEmpty( )) return false;
		else return true;
		
	}
	
	public int size( ) {
		
		// return the size of the loggerQueue
		return loggerQueue.size();
		
	}
	
	public String poll( long timeout ) {
		
		// Retrieve and remove the head of the loggerQueue, or return null if the dispatchQueue is empty
		try {
			return loggerQueue.poll( timeout, TimeUnit.SECONDS );
		} catch (InterruptedException e) {
			LOGGER.fine("Logger Queue poll interrupted.");
		}
		return null;
		
	}

}
