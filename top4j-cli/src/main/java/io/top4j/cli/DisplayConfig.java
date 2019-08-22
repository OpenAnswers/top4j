/*
 * Copyright (c) 2019 Open Answers Ltd.
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

package io.top4j.cli;

public class DisplayConfig {

    private int threadCount;
    private int jvmPid;
    private String displayName;

    public DisplayConfig(int threadCount, int jvmPid, String displayName) {
        this.threadCount = threadCount;
        this.jvmPid = jvmPid;
        this.displayName = displayName;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getJvmPid() {
        return jvmPid;
    }

    public void setJvmPid(int jvmPid) {
        this.jvmPid = jvmPid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
