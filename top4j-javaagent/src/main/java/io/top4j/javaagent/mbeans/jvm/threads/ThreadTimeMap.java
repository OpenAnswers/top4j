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
			
		} else {
			
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
