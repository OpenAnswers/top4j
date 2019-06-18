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
