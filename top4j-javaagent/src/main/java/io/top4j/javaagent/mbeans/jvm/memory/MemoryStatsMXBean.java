package io.top4j.javaagent.mbeans.jvm.memory;

import io.top4j.javaagent.mbeans.StatsMXBean;

public interface MemoryStatsMXBean extends StatsMXBean {
	
	void setMemoryAllocationRate(double memoryAllocationRate);

	double getMemoryAllocationRate();
	
	void setMemorySurvivorRate(double memorySurvivorRate);

	double getMemorySurvivorRate();
	
	void setMemoryPromotionRate(double memoryPromotionRate);

	double getMemoryPromotionRate();
	
}
