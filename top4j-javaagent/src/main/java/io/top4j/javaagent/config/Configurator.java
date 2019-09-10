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

package io.top4j.javaagent.config;

import javax.management.MBeanServerConnection;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;


public class Configurator {

    public MBeanServerConnection mBeanServerConnection;
	private Map<String, String> config = new HashMap<>();
	private boolean statsLoggerEnabled;
	private boolean topThreadsStackTraceLoggingEnabled;
	private boolean blockedThreadsStackTraceLoggingEnabled;
	private boolean threadContentionMonitoringEnabled;
    private boolean hotMethodProfilingEnabled;
    private boolean threadUsageCacheEnabled;

	private static final Logger LOGGER = Logger.getLogger(Configurator.class.getName());

	public Configurator ( MBeanServerConnection mBeanServerConnection, String agentArgs ) {

        // store MBean server connection
        this.mBeanServerConnection = mBeanServerConnection;
		
        // load default properties
        loadPropsFromClasspath(Constants.DEFAULT_PROPERTIES_FILE_NAME);

        // load test properties
        loadPropsFromClasspath(Constants.TEST_PROPERTIES_FILE_NAME);

        // load file system properties
        loadPropsFromFileSystem(Constants.PROPERTIES_FILE_NAME);

        // load agent args
        loadAgentArgs(agentArgs);

        // load agent arg properties
        if (config.get(Constants.PROPERTIES_FILE_NAME_ARG) != null) {
            loadPropsFromFileSystem( config.get(Constants.PROPERTIES_FILE_NAME_ARG) );
        }

        if (Boolean.parseBoolean(config.get("log.properties.on.startup"))) {
            // list all configured properties and their values
            listProperties();
        }

    	// set statsLoggerEnabled status
        this.statsLoggerEnabled = Boolean.parseBoolean(config.get("stats.logger.enabled"));
    	
    	// set topThreadsStackTraceLoggingEnabled status
        this.topThreadsStackTraceLoggingEnabled = Boolean.parseBoolean(config.get("top.threads.stack.trace.logging.enabled"));

		// set blockedThreadsStackTraceLoggingEnabled status
        this.blockedThreadsStackTraceLoggingEnabled = Boolean.parseBoolean(config.get("blocked.threads.stack.trace.logging.enabled"));

		// set threadContentionMonitoringEnabled status
        this.threadContentionMonitoringEnabled = Boolean.parseBoolean(config.get("thread.contention.monitoring.enabled"));

        // set hotMethodProfilingEnabled status
        this.hotMethodProfilingEnabled = Boolean.parseBoolean(config.get("hot.method.profiling.enabled"));

        // set threadUsageCacheEnabled status
        this.threadUsageCacheEnabled = Boolean.parseBoolean(config.get("thread.usage.cache.enabled"));
	}

    private void loadPropsFromClasspath ( String propsFileName ) {

        Properties props = new Properties();

        try {
            // load properties file from class path
            InputStream is = getClass().getResourceAsStream("/" + propsFileName);
            if (is != null) {
                props.load(is);
                // load test properties into config Map
                loadProps(props, propsFileName);
                LOGGER.info("Top4J: Loaded " + propsFileName + " file from classpath.");
            }

        } catch (IOException e) {

            LOGGER.fine("Unable to load " + propsFileName + " file from the classpath.");
        }

    }

    private void loadPropsFromFileSystem ( String propsFileName ) {

        Properties props = new Properties();

        try {
            // attempt to load properties file from the file system
            props.load(new FileInputStream(propsFileName));
            // load file system properties into config Map
            loadProps(props, propsFileName);
            LOGGER.info("Top4J: Loaded override properties " + propsFileName + " from file system.");

        } catch (IOException ex) {

            LOGGER.fine("No properties to load from the file system.");
        }

    }

	private void loadProps ( Properties props, String propsFileName ) {

		for (final String name: props.stringPropertyNames()) {

            String value = props.getProperty(name);
            LOGGER.fine("Top4J Property Loader: " + propsFileName + ": " + name + " = " + value);
            config.put(name, value);

        }

	}

    private void loadAgentArgs(String agentArgs) {

        if (agentArgs != null) {
            // parse agent args and load into config Map
            for(String propertyAndValue : agentArgs.split(",")) {
                String[] tokens = propertyAndValue.split("=", 2);
                if (tokens.length != 2) {
                    continue;
                }
                config.put(tokens[0], tokens[1]);
            }
        }
    }

    public void listProperties( ) {

        // list all configured properties and their values
        for (Map.Entry<String, String> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            LOGGER.info("Top4J Properties: " + key + " = " + value);
        }
    }

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}

	public Map<String, String> getConfig() {
		return config;
	}
	
	public String get(String prop) {
		
		return this.config.get(prop);
		
	}

	public boolean isStatsLoggerEnabled() {
		return this.statsLoggerEnabled;
	}
	
	public boolean isTopThreadsStackTraceLoggingEnabled() {
		return this.topThreadsStackTraceLoggingEnabled;
	}

	public boolean isBlockedThreadsStackTraceLoggingEnabled() {
		return this.blockedThreadsStackTraceLoggingEnabled;
	}

	public boolean isThreadContentionMonitoringEnabled() {
		return this.threadContentionMonitoringEnabled;
	}

    public boolean isHotMethodProfilingEnabledEnabled() {
        return this.hotMethodProfilingEnabled;
    }

    public boolean isHotMethodStackTraceLoggingEnabled() {
        return this.topThreadsStackTraceLoggingEnabled;
    }

    public boolean isThreadUsageCacheEnabled() {
        return this.threadUsageCacheEnabled;
    }

}
