package io.top4j.javaagent.mbeans.agent;

import io.top4j.javaagent.mbeans.StatsMXBean;

public interface AgentStatsMXBean extends StatsMXBean {

	void setAgentCpuTime(double cpuTime);

	double getAgentCpuTime();

	void setAgentCpuUtil(double cpuUtil);
	
	double getAgentCpuUtil();
	
	void setIterations(long iterations);
	
	long getIterations();
	
}
