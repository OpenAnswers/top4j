package io.top4j.javaagent.mbeans.jvm.heap;

import io.top4j.javaagent.mbeans.StatsMXBean;

/**
 * Created by ryan on 02/06/15.
 */
public interface HeapStatsMXBean extends StatsMXBean {

	void setEdenSpaceUtil(double edenSpaceUtil);

	double getEdenSpaceUtil();

	void setSurvivorSpaceUtil(double survivorSpaceUtil);

	double getSurvivorSpaceUtil();

	void setTenuredHeapUtil(double tenuredHeapUtil);

	double getTenuredHeapUtil();

}
