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

public class MemorySurvivorBean {

    private long survivors;
    private long lastGCCount;
    private long lastSystemTime;

    public MemorySurvivorBean() {

        this.setSurvivors(0);
        this.setLastGCCount(0);

    }

    public synchronized long getSurvivors() {
        return survivors;
    }

    public synchronized void setSurvivors(long survivors) {
        this.survivors = survivors;
    }

    public synchronized void resetSurvivors() {
        this.survivors = 0;
    }

    public synchronized void addSurvivors(long survivors) {
        this.survivors += survivors;
    }

    public synchronized long getAndResetSurvivors() {

        long survivors = this.survivors;
        this.survivors = 0;
        return survivors;

    }

    public long getLastGCCount() {
        return lastGCCount;
    }

    public void setLastGCCount(long lastGCCount) {
        this.lastGCCount = lastGCCount;
    }

    public long getLastSystemTime() {
        return lastSystemTime;
    }

    public void setLastSystemTime(long lastSystemTime) {
        this.lastSystemTime = lastSystemTime;
    }

}
