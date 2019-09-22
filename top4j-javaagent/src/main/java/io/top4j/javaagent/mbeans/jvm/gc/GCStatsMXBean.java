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

package io.top4j.javaagent.mbeans.jvm.gc;

import io.top4j.javaagent.mbeans.StatsMXBean;

/**
 * Used to store and expose stats relating to the performance of the JVM Garbage Collector.
 */

public interface GCStatsMXBean extends StatsMXBean {

    /**
     * Sets the GC overhead which is calculated as the percentage of real time (wall clock time) the JVM spends in garbage collection.
     * <p>
     * Only stop-the-world garbage collection pauses contribute to the GC overhead. This, therefore, equates to the percentage of real time
     * that the application is stopped whilst garbage collection takes place. This is a key performance indicator of the impact of garbage collection
     * on a running Java application. A high GC overhead overhead can lead to poor application performance as there is less time available to process
     * application tasks and application threads can be blocked waiting to allocate memory (i.e. create objects).
     * @param gcOverhead the GC overhead
     */
    void setGcOverhead(double gcOverhead);

    /**
     * Returns the GC overhead which is calculated as the percentage of real time (wall clock time) the JVM spends in garbage collection.
     * <p>
     * Only stop-the-world garbage collection pauses contribute to the GC overhead. This, therefore, equates to the percentage of real time
     * that the application is stopped whilst garbage collection takes place. This is a key performance indicator of the impact of garbage collection
     * on a running Java application. A high GC overhead overhead can lead to poor application performance as there is less time available to process
     * application tasks and application threads can be blocked waiting to allocate memory (i.e. create objects).
     * @return the GC overhead
     */
    double getGcOverhead();

    /**
     * Sets the mean time in milliseconds spent during a single nursery or eden or new stop-the-world GC event during the last iteration.
     * This time is not available for application processing and should therefore be kept to a minimum.
     * @param meanNurseryGCTime the mean nursery GC time in milliseconds
     */
    void setMeanNurseryGCTime(double meanNurseryGCTime);

    /**
     * Returns the mean time in milliseconds spent during a single nursery or eden or new stop-the-world GC event during the last iteration.
     * This time is not available for application processing and should therefore be kept to a minimum.
     * @return the mean nursery GC time in milliseconds
     */
    double getMeanNurseryGCTime();

    /**
     * Sets the mean time in milliseconds spent during a single tenured or full or old stop-the-world GC event during the last iteration.
     * This time is not available for application processing and should therefore be kept to a minimum.
     * @param meanTenuredGCTime the mean tenured GC time in milliseconds
     */
    void setMeanTenuredGCTime(double meanTenuredGCTime);

    /**
     * Returns the mean time in milliseconds spent during a single tenured or full or old stop-the-world GC event during the last iteration.
     * This time is not available for application processing and should therefore be kept to a minimum.
     * @return the mean tenured GC time in milliseconds
     */
    double getMeanTenuredGCTime();

}
