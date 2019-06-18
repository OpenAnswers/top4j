package io.top4j.javaagent.controller;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.logging.*;

import io.top4j.javaagent.config.Configurator;

import javax.management.MBeanServer;

public class Agent {
	
	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Agent.class.getName());
	
	public static void premain(String agentArgs, Instrumentation inst) {

		// get platform MBean server
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		// initialise configurator
		Configurator config = new Configurator( mbs, agentArgs );
		
		// create and start controller thread
		Controller controller = new Controller( config );
		controller.start( );

		logger.info("Top4J: Java agent activated.");

	}

}
