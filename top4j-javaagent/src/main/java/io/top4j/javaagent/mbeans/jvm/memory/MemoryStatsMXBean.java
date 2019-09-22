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

package io.top4j.javaagent.mbeans.jvm.memory;

import io.top4j.javaagent.mbeans.StatsMXBean;

/**
 * Used to store and expose stats relating to the JVM memory pool usage.
 */

public interface MemoryStatsMXBean extends StatsMXBean {

    /**
     * Sets the memory allocation rate which represents the amount of memory consumed by the Java application whilst creating new objects over time.
     * It is measured in MB per second (MB/s). A high memory allocation rate can be an indication that the Java application is creating too many new
     * objects and as a result putting pressure on the JVM memory management sub-system which can cause more frequent GC events and associated GC overhead.
     * @param memoryAllocationRate the memory allocation rate in MB/s
     */
    void setMemoryAllocationRate(double memoryAllocationRate);

    /**
     * Returns the memory allocation rate which represents the amount of memory consumed by the Java application whilst creating new objects over time.
     * It is measured in MB per second (MB/s). A high memory allocation rate can be an indication that the Java application is creating too many new
     * objects and as a result putting pressure on the JVM memory management sub-system which can cause more frequent GC events and associated GC overhead.
     * @return the memory allocation rate in MB/s
     */
    double getMemoryAllocationRate();

    /**
     * Sets the memory survivor rate represents the amount of memory that survives a nursery (or new) GC event and is promoted to one of the survivor spaces over time.
     * It is measured in MB per second (MB/s). A high memory survivor rate can be an indication that too many objects are being promoted to the survivor spaces which
     * can be an indication that the eden space is undersized or the memory allocation rate (to eden) is too high.
     * @param memorySurvivorRate the memory survivor rate in MB/s
     */
    void setMemorySurvivorRate(double memorySurvivorRate);

    /**
     * Returns the memory survivor rate represents the amount of memory that survives a nursery (or new) GC event and is promoted to one of the survivor spaces over time.
     * It is measured in MB per second (MB/s). A high memory survivor rate can be an indication that too many objects are being promoted to the survivor spaces which
     * can be an indication that the eden space is undersized or the memory allocation rate (to eden) is too high.
     * @return the memory survivor rate in MB/s
     */
    double getMemorySurvivorRate();

    /**
     * Sets the memory promotion rate represents the amount of memory that survives one or more nursery (or new) GC events and is promoted to the tenured (or old) space over time.
     * It is measured in MB per second (MB/s). A high memory promotion rate can be an indication that too many objects are being promoted to the tenured space which
     * can be an indication that the eden space is undersized or the memory allocation rate (to eden) is too high.
     * @param memoryPromotionRate the memory promotion rate in MB/s
     */
    void setMemoryPromotionRate(double memoryPromotionRate);

    /**
     * Returns the memory promotion rate represents the amount of memory that survives one or more nursery (or new) GC events and is promoted to the tenured (or old) space over time.
     * It is measured in MB per second (MB/s). A high memory promotion rate can be an indication that too many objects are being promoted to the tenured space which
     * can be an indication that the eden space is undersized or the memory allocation rate (to eden) is too high.
     * @return the memory promotion rate in MB/s
     */
    double getMemoryPromotionRate();

}
