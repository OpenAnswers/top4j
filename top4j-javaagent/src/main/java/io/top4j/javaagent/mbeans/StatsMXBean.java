package io.top4j.javaagent.mbeans;

/**
 * Created by ryan on 17/08/15.
 */
public interface StatsMXBean {

    void update();

    void setMBeanCpuTime(double mBeanCpuTime);

    double getMBeanCpuTime();

}
