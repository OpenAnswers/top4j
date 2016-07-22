package io.top4j.javaagent.mbeans.jvm.memory;
	
import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.*;
import java.util.List;

public class MemoryPoolUsageBean {
		
		private long lastCollectionUsageUsed;
		private long lastCollectionUsageInit;
		private long lastCollectionUsageCommitted;
		private long lastCollectionUsageMax;
		private long lastMemoryPoolUsageUsed;
		private long lastMemoryPoolUsageInit;
		private long lastMemoryPoolUsageCommitted;
		private long lastMemoryPoolUsageMax;
		private long lastSystemTime;
		
		public MemoryPoolUsageBean ( MBeanServerConnection mbsc, String memoryPoolName ) throws IOException {
			
			final List<MemoryPoolMXBean> memPoolMXBeans =
					ManagementFactory.getPlatformMXBeans( mbsc, MemoryPoolMXBean.class );
			String mpName;
			MemoryUsage currentCollectionMemoryUsage;
			MemoryUsage currentMemoryPoolMemoryUsage;
			long currentCollectionUsageUsed = 0;
			long currentCollectionUsageInit = 0;
			long currentCollectionUsageCommitted = 0;
			long currentCollectionUsageMax = 0;
			long currentMemoryPoolUsageUsed = 0;
			long currentMemoryPoolUsageInit = 0;
			long currentMemoryPoolUsageCommitted = 0;
			long currentMemoryPoolUsageMax = 0;
			for (MemoryPoolMXBean memPoolMXBean : memPoolMXBeans) {
				mpName = memPoolMXBean.getName();
				if (mpName.equals(memoryPoolName)) {
					currentCollectionMemoryUsage = memPoolMXBean.getCollectionUsage();
					currentCollectionUsageUsed = currentCollectionMemoryUsage.getUsed();
					currentCollectionUsageInit = currentCollectionMemoryUsage.getInit();
					currentCollectionUsageCommitted = currentCollectionMemoryUsage.getCommitted();
					currentCollectionUsageMax = currentCollectionMemoryUsage.getMax();
					
					currentMemoryPoolMemoryUsage = memPoolMXBean.getUsage();
					currentMemoryPoolUsageUsed = currentMemoryPoolMemoryUsage.getUsed();
					currentMemoryPoolUsageInit = currentMemoryPoolMemoryUsage.getInit();
					currentMemoryPoolUsageCommitted = currentMemoryPoolMemoryUsage.getCommitted();
					currentMemoryPoolUsageMax = currentMemoryPoolMemoryUsage.getMax();
				}
			}
			
			this.setLastCollectionUsageUsed(currentCollectionUsageUsed);
			this.setLastCollectionUsageInit(currentCollectionUsageInit);
			this.setLastCollectionUsageCommitted(currentCollectionUsageCommitted);
			this.setLastCollectionUsageMax(currentCollectionUsageMax);
			this.setLastMemoryPoolUsageUsed(currentMemoryPoolUsageUsed);
			this.setLastMemoryPoolUsageInit(currentMemoryPoolUsageInit);
			this.setLastMemoryPoolUsageCommitted(currentMemoryPoolUsageCommitted);
			this.setLastMemoryPoolUsageMax(currentMemoryPoolUsageMax);
			this.setLastSystemTime(System.currentTimeMillis());
			
		}

		public void setLastCollectionUsageUsed(long lastCollectionUsageUsed) {
			this.lastCollectionUsageUsed = lastCollectionUsageUsed;
		}

		public long getLastCollectionUsageUsed() {
			return lastCollectionUsageUsed;
		}

		public void setLastCollectionUsageInit(long lastCollectionUsageInit) {
			this.lastCollectionUsageInit = lastCollectionUsageInit;
		}

		public long getLastCollectionUsageInit() {
			return lastCollectionUsageInit;
		}

		public void setLastCollectionUsageMax(long lastCollectionUsageMax) {
			this.lastCollectionUsageMax = lastCollectionUsageMax;
		}

		public long getLastCollectionUsageMax() {
			return lastCollectionUsageMax;
		}

		public void setLastMemoryPoolUsageUsed(long lastMemoryPoolUsageUsed) {
			this.lastMemoryPoolUsageUsed = lastMemoryPoolUsageUsed;
		}

		public long getLastMemoryPoolUsageUsed() {
			return lastMemoryPoolUsageUsed;
		}

		public void setLastMemoryPoolUsageInit(long lastMemoryPoolUsageInit) {
			this.lastMemoryPoolUsageInit = lastMemoryPoolUsageInit;
		}

		public long getLastMemoryPoolUsageInit() {
			return lastMemoryPoolUsageInit;
		}

		public void setLastMemoryPoolUsageMax(long lastMemoryPoolUsageMax) {
			this.lastMemoryPoolUsageMax = lastMemoryPoolUsageMax;
		}

		public long getLastMemoryPoolUsageMax() {
			return lastMemoryPoolUsageMax;
		}

		public void setLastSystemTime(long lastSystemTime) {
			this.lastSystemTime = lastSystemTime;
		}

		public long getLastSystemTime() {
			return lastSystemTime;
		}

		public void setLastCollectionUsageCommitted(long lastCollectionUsageCommitted) {
			this.lastCollectionUsageCommitted = lastCollectionUsageCommitted;
		}

		public long getLastCollectionUsageCommitted() {
			return lastCollectionUsageCommitted;
		}

		public void setLastMemoryPoolUsageCommitted(long lastMemoryPoolUsageCommitted) {
			this.lastMemoryPoolUsageCommitted = lastMemoryPoolUsageCommitted;
		}

		public long getLastMemoryPoolUsageCommitted() {
			return lastMemoryPoolUsageCommitted;
		}

}
