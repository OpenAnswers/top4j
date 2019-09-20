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

package io.top4j.javaagent.config;

public final class Constants {

    // JMX MXBean domain name
    public static final String DOMAIN = "io.top4j";

    // JMX MXBean JVM stats type
    public static final String JVM_STATS_TYPE = "JVM";

    // JMX MXBean top thread stats type
    public static final String TOP_THREAD_STATS_TYPE = "TopThread";

    // JMX MXBean blocked thread stats type
    public static final String BLOCKED_THREAD_STATS_TYPE = "BlockedThread";

    // JMX MXBean hot method stats type
    public static final String HOT_METHOD_STATS_TYPE = "HotMethod";

    // JMX MXBean threads stats type
    public static final String THREADS_STATS_TYPE = "ThreadStats";

    // JMX MXBean memory stats type
    public static final String MEMORY_STATS_TYPE = "MemoryStats";

    // JMX MXBean heap stats type
    public static final String HEAP_STATS_TYPE = "HeapStats";

    // JMX MXBean GC stats type
    public static final String GC_STATS_TYPE = "GCStats";

    // JMX MXBean agent type
    public static final String AGENT_TYPE = "Agent";

    // JMX MXBean agent stats type
    public static final String AGENT_STATS_TYPE = "AgentStats";

    // JMX MXBean stats logger type
    public static final String STATS_LOGGER_TYPE = "StatsLogger";

    // file path separator
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    // default properties file name
    public static final String DEFAULT_PROPERTIES_FILE_NAME = "default-top4j.properties";

    // properties file name
    public static final String PROPERTIES_FILE_NAME = "top4j.properties";

    // test properties file name
    public static final String TEST_PROPERTIES_FILE_NAME = "top4j-test.properties";

    // properties file name argument token (used to specify properties file location via agent argument)
    public static final String PROPERTIES_FILE_NAME_ARG = "config.file";

    // 1 Mega Byte
    public static final int ONE_MEGA_BYTE = 1048576;

    private Constants() {
        throw new AssertionError();
    }
}
