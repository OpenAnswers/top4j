package io.top4j.javaagent.mbeans.jvm.memory;

public class MemorySurvivorBean {
	
	private long survivors;
	private long lastGCCount;
	private long lastSystemTime;
	
	public MemorySurvivorBean ( ) {
		
		this.setSurvivors(0);
		this.setLastGCCount(0);
		
	}

	public synchronized long getSurvivors() {
		return survivors;
	}

	public synchronized void setSurvivors(long survivors) {
		this.survivors = survivors;
	}
	
	public synchronized void resetSurvivors() {
		this.survivors = 0;
	}
	
	public synchronized void addSurvivors(long survivors) {
		this.survivors += survivors;
	}
	
	public synchronized long getAndResetSurvivors() {
		
		long survivors = this.survivors;
		this.survivors = 0;
		return survivors;
		
	}

	public long getLastGCCount() {
		return lastGCCount;
	}

	public void setLastGCCount(long lastGCCount) {
		this.lastGCCount = lastGCCount;
	}

	public long getLastSystemTime() {
		return lastSystemTime;
	}

	public void setLastSystemTime(long lastSystemTime) {
		this.lastSystemTime = lastSystemTime;
	}

}
