package io.top4j.javaagent.mbeans.jvm.threads;

import io.top4j.javaagent.profiler.CpuTime;

import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Created by ryan on 02/08/15.
 */
public class HotMethodTracker extends TimerTask {

    private Map<Integer, TopThread> topThreadMap;
    private HotMethods hotMethods;
    private CpuTime cpuTime = new CpuTime();

    private static final Logger LOGGER = Logger.getLogger(HotMethodTracker.class.getName());

    public HotMethodTracker( Map<Integer, TopThread> topThreadMap, HotMethods hotMethods ) {

        // store topThreadMap
        this.topThreadMap = topThreadMap;
        // store hotMethods
        this.hotMethods = hotMethods;

    }

    @Override
    public void run() {

        // initialise thread CPU timer
        cpuTime.init();

        // iterate over topThreadsMap
        for ( TopThread topThread : topThreadMap.values() ) {

            // get topThread stack trace
            StackTraceElement[] stackTrace = topThread.getStackTraceElements();
            // get topThread name
            String threadName = topThread.getThreadName();
            // get topThread Id
            long threadId = topThread.getThreadId();
            // get topThread state
            Thread.State threadState = topThread.getThreadState();
            if (stackTrace != null && threadName != null && threadId != 0 && threadState.equals(Thread.State.RUNNABLE)) {
                // add top thread stack trace, name and ID to hotMethods
                hotMethods.addHotMethod(topThread.getStackTraceElements(), topThread.getThreadName(), topThread.getThreadId());
            }
        }

        LOGGER.finer("Hot Method Tracker CPU Time: " + cpuTime.getMillis() + " ms");
        // update hot method tracker CPU time
        hotMethods.addMBeanCpuTime(cpuTime.getMillis());
    }
}
