package io.top4j.javaagent.mbeans.agent;

import io.top4j.javaagent.mbeans.StatsMXBean;
import io.top4j.javaagent.profiler.CpuTime;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import java.util.logging.Logger;

public class AgentStats implements AgentStatsMXBean {

	volatile private double agentCpuTime;
	volatile private double agentCpuUtil;
	volatile private double mBeanCpuTime;
	volatile private long iterations;
	private int availableProcessors;
	private long lastCpuTime;
	private long lastSystemTime;
	private CpuTime cpuTime = new CpuTime();
	private List<StatsMXBean> jvmStatsMBeans;

	private static final Logger LOGGER = Logger.getLogger(AgentStats.class.getName());
	
	public AgentStats ( List<StatsMXBean> jvmStatsMBeans ) {
		
		this.setAgentCpuUtil(0);
		this.setMBeanCpuTime(0);
		this.setIterations(0);
		this.setLastCpuTime(0);
		long systemTime = System.currentTimeMillis();
		this.setLastSystemTime( systemTime );
        this.jvmStatsMBeans = jvmStatsMBeans;
		
		final OperatingSystemMXBean osbean =
	            ManagementFactory.getOperatingSystemMXBean();
		this.setAvailableProcessors(osbean.getAvailableProcessors());

	}

	@Override
	public void setAgentCpuTime(double cpuTime) {
		this.agentCpuTime = cpuTime;
	}

	@Override
	public double getAgentCpuTime() {
		return agentCpuTime;
	}

	@Override
	public void setAgentCpuUtil(double agentCpuUtil) {
		this.agentCpuUtil = agentCpuUtil;

	}

	@Override
	public double getAgentCpuUtil() {
		return agentCpuUtil;
	}

	@Override
	public void setIterations(long iterations) {
		this.iterations = iterations;
	}

	@Override
	public long getIterations() {
		return iterations;
	}

	@Override
	/* Update Agent Stats */
	public synchronized void update() {

		// initialise thread CPU timer
		cpuTime.init();

		LOGGER.fine("Updating Agent stats....");
		
		// update CPU util
		updateCpuUtil();
		
		// update iterations
		updateIterations();

		// update agent stats CPU time
		mBeanCpuTime = cpuTime.getMillis();

	}
	
	private void updateCpuUtil() {

		// get current system time
		long systemTime = System.currentTimeMillis();
		// calculate time difference since last update
		long timeDiffMillis = systemTime - lastSystemTime;
        // aggregate agentCpuTime in milliseconds accumulated during this iteration
        double agentCpuTime = 0;
        for (StatsMXBean jvmStats : jvmStatsMBeans) {
            agentCpuTime+=jvmStats.getMBeanCpuTime();
        }
        double threadCpuUsage = ( agentCpuTime / timeDiffMillis) * 100;
        double cpuUtil = threadCpuUsage / availableProcessors;
        LOGGER.fine("Agent CPU Util = " + cpuUtil + "%");
        LOGGER.fine("Agent CPU Time = " + agentCpuTime + "ms");
		// update agent CPU util
		this.agentCpuUtil = cpuUtil;
		// update agent CPU time
		this.agentCpuTime = agentCpuTime;
		// update last system time
		this.lastSystemTime = systemTime;
	}
	
	private void updateIterations() {
		
		// update iterations
		this.iterations++;
	}

	public long getLastCpuTime() {
		return lastCpuTime;
	}

	public void setLastCpuTime(long lastCpuTime) {
		this.lastCpuTime = lastCpuTime;
	}

	public int getAvailableProcessors() {
		return availableProcessors;
	}

	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	public long getLastSystemTime() {
		return lastSystemTime;
	}

	public void setLastSystemTime(long lastSystemTime) {
		this.lastSystemTime = lastSystemTime;
	}

	@Override
	public void setMBeanCpuTime(double agentCpuTime) {
		this.mBeanCpuTime = agentCpuTime;
		
	}

	@Override
	public double getMBeanCpuTime() {
		return mBeanCpuTime;
	}

}
