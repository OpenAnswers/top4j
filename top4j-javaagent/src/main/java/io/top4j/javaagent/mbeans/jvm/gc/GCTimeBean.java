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

package io.top4j.javaagent.mbeans.jvm.gc;
	
import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.*;
import java.util.List;

public class GCTimeBean {
		
		private long lastGCTime;
		private long lastGCCount;
		private long lastNurseryGCTime;
		private long lastNurseryGCCount;
		private long lastTenuredGCTime;
		private long lastTenuredGCCount;
		private long lastSystemTime;
		
		public GCTimeBean ( MBeanServerConnection mbsc ) throws IOException {
			
			final List<GarbageCollectorMXBean> gcbeans =
	            ManagementFactory.getPlatformMXBeans( mbsc, GarbageCollectorMXBean.class );
			long currentGCTime = 0;
			long currentGCCount = 0;
			long currentNurseryGCTime = 0;
			long currentNurseryGCCount = 0;
			long currentTenuredGCTime = 0;
			long currentTenuredGCCount = 0;
			for (GarbageCollectorMXBean gcbean : gcbeans) {
				String name = gcbean.getName();
				if (name.equals("Copy") || name.equals("PS Scavenge")) {
					currentNurseryGCTime = gcbean.getCollectionTime();
					currentNurseryGCCount = gcbean.getCollectionCount();
				}
				if (name.equals("MarkSweepCompact") || name.equals("PS MarkSweep")) {
					currentTenuredGCTime = gcbean.getCollectionTime();
					currentTenuredGCCount = gcbean.getCollectionCount();
				}
				currentGCTime += gcbean.getCollectionTime();
				currentGCCount += gcbean.getCollectionCount();
			}
			this.setLastGCTime(currentGCTime);
			this.setLastGCCount(currentGCCount);
			this.setLastNurseryGCTime(currentNurseryGCTime);
			this.setLastNurseryGCCount(currentNurseryGCCount);
			this.setLastTenuredGCTime(currentTenuredGCTime);
			this.setLastTenuredGCCount(currentTenuredGCCount);
			this.setLastSystemTime(System.currentTimeMillis());
			
		}

		public void setLastGCTime(long lastGCTime) {
			this.lastGCTime = lastGCTime;
		}

		public long getLastGCTime() {
			return lastGCTime;
		}

		public void setLastSystemTime(long lastSystemTime) {
			this.lastSystemTime = lastSystemTime;
		}

		public long getLastSystemTime() {
			return lastSystemTime;
		}

		public void setLastGCCount(long lastGCCount) {
			this.lastGCCount = lastGCCount;
		}

		public long getLastGCCount() {
			return lastGCCount;
		}

		public void setLastNurseryGCTime(long lastNurseryGCTime) {
			this.lastNurseryGCTime = lastNurseryGCTime;
		}

		public long getLastNurseryGCTime() {
			return lastNurseryGCTime;
		}

		public void setLastNurseryGCCount(long lastNurseryGCCount) {
			this.lastNurseryGCCount = lastNurseryGCCount;
		}

		public long getLastNurseryGCCount() {
			return lastNurseryGCCount;
		}

		public void setLastTenuredGCTime(long lastTenuredGCTime) {
			this.lastTenuredGCTime = lastTenuredGCTime;
		}

		public long getLastTenuredGCTime() {
			return lastTenuredGCTime;
		}

		public void setLastTenuredGCCount(long lastTenuredGCCount) {
			this.lastTenuredGCCount = lastTenuredGCCount;
		}

		public long getLastTenuredGCCount() {
			return lastTenuredGCCount;
		}

}
