package io.top4j.javaagent.mbeans.jvm.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;

public class ThreadTimeMap {

	// Map key = thread time, Map value = list of thread IDs
	NavigableMap<Long, List<Long>> threadTimeMap = new TreeMap<>();
	
	public void put( Long threadTime, Long threadId ) {
		
		// get list of thread IDs with this threadTime
		List<Long> existingThreadIdList = threadTimeMap.get(threadTime);
		
		if ( existingThreadIdList == null ) {
			
			// create new threadIdList
			List<Long> newThreadIdList = new ArrayList<>();
			// add threadId to threadIdList
			newThreadIdList.add(threadId);
			// add threadTime and threadIdList to threadTimeMap
			threadTimeMap.put(threadTime, newThreadIdList);
			
		}
		
		else {
			
			// add threadId to threadIdList
			existingThreadIdList.add(threadId);
			// add threadTime and threadIdList to threadTimeMap
			threadTimeMap.put(threadTime, existingThreadIdList);
			
		}
		
	}
	
	public List<Long> get( Long threadTime) {
		
		return threadTimeMap.get(threadTime);
		
	}
	
	public int size( ) {
		
		return threadTimeMap.size();
		
	}
	
	public NavigableSet<Long> descendingKeySet( ) {
		
		return threadTimeMap.descendingKeySet();
		
	}
	
	public String toString( ) {
		
		return threadTimeMap.toString();
		
	}
	
	public NavigableMap<Long, List<Long>> getThreadTimeMap() {
		
		return threadTimeMap;
		
	}

}
