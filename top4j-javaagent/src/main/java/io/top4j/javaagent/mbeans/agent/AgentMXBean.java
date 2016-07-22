package io.top4j.javaagent.mbeans.agent;

public interface AgentMXBean {
	
	void stop();
	
	void start();
	
	String setStatus(String status);
	
	String getStatus();

}
