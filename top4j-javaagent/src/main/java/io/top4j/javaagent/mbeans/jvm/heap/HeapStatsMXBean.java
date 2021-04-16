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

package io.top4j.javaagent.mbeans.jvm.heap;

import io.top4j.javaagent.mbeans.StatsMXBean;

/**
 * Used to store and expose stats relating to the JVM heap utilisation. The heap utilisation is calculated as the percentage of heap used following the most recent garbage collection event. In other words.... ( heapUsed / heapCommitted ) * 100.
 */

public interface HeapStatsMXBean extends StatsMXBean {

    /**
     * Sets the eden (or nursery or new) heap space utilisation following the most recent garbage collection event.
     * <p>
     * This is effectively the residual heap occupied by live objects within the eden space which can't be garbage collected because they are
     * still referenced by one or more other objects. The eden heap utilisation is typically very low as most objects created within the eden
     * heap space are either garbage collected (cleared up) or promoted to one of the survivor spaces at each nursery GC event.
     * @param edenSpaceUtil the eden heap space utilisation
     */
    void setEdenSpaceUtil(double edenSpaceUtil);

    /**
     * Returns the eden (or nursery or new) heap space utilisation following the most recent garbage collection event.
     * <p>
     * This is effectively the residual heap occupied by live objects within the eden space which can't be garbage collected because they are
     * still referenced by one or more other objects. The eden heap utilisation is typically very low as most objects created within the eden
     * heap space are either garbage collected (cleared up) or promoted to one of the survivor spaces at each nursery GC event.
     * @return the eden heap space utilisation
     */
    double getEdenSpaceUtil();

    /**
     * Sets the survivor heap space utilisation following the most recent garbage collection event.
     * <p>
     * This is effectively the residual heap occupied by live objects within the survivor spaces which can't be garbage collected because they are
     * still referenced by one or more other objects.
     * @param survivorSpaceUtil the survivor heap space utilisation
     */
    void setSurvivorSpaceUtil(double survivorSpaceUtil);

    /**
     * Returns the survivor heap space utilisation following the most recent garbage collection event.
     * <p>
     * This is effectively the residual heap occupied by live objects within the survivor spaces which can't be garbage collected because they are
     * still referenced by one or more other objects.
     * @return the survivor heap space utilisation
     */
    double getSurvivorSpaceUtil();

    /**
     * Sets the tenured (or old) heap space utilisation following the most recent garbage collection event.
     * This is effectively the residual heap occupied by live objects within the tenured (or old) space which can't be garbage collected because they are
     * still referenced by one or more other objects. High tenured heap space utilisation can be an indication that the JVM is running low on memory.
     * A high tenured heap utilisation can lead to frequent garbage collection events which will typically lead to a high GC overhead and therefore poor
     * application performance/memory throughput. See GCOverhead attribute above for more details.
     * @param tenuredHeapUtil the tenured heap space utilisation
     */
    void setTenuredHeapUtil(double tenuredHeapUtil);

    /**
     * Returns the tenured (or old) heap space utilisation following the most recent garbage collection event.
     * <p>
     * This is effectively the residual heap occupied by live objects within the tenured (or old) space which can't be garbage collected because they are
     * still referenced by one or more other objects. High tenured heap space utilisation can be an indication that the JVM is running low on memory.
     * A high tenured heap utilisation can lead to frequent garbage collection events which will typically lead to a high GC overhead and therefore poor
     * application performance/memory throughput. See GCOverhead attribute above for more details.
     * @return the tenured heap space utilisation
     */
    double getTenuredHeapUtil();

    /**
     * @return true if the heap is a single-generation (thus no survivor/tenured generations).
     */
    boolean isSingleGenerationHeap();

}
