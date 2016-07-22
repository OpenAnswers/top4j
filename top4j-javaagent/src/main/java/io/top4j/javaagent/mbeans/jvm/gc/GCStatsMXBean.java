package io.top4j.javaagent.mbeans.jvm.gc;

import io.top4j.javaagent.mbeans.StatsMXBean;

public interface GCStatsMXBean extends StatsMXBean {
	
	void setGcOverhead(double gcOverhead);

	double getGcOverhead();

	void setMeanNurseryGCTime(double meanNurseryGCTime);
	
	double getMeanNurseryGCTime();

	void setMeanTenuredGCTime(double meanTenuredGCTime);

	double getMeanTenuredGCTime();
	
}
