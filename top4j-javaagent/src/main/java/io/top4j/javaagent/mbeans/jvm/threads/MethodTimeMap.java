package io.top4j.javaagent.mbeans.jvm.threads;

import java.util.*;

public class MethodTimeMap {

	// Map key = method count, Map value = list of method names
	NavigableMap<Long, List<String>> methodTimeMap = new TreeMap<>();
	
	public void put( Long methodTime, String methodName ) {
		
		// get list of method names with this methodTime
		List<String> existingMethodNameList = methodTimeMap.get(methodTime);
		
		if ( existingMethodNameList == null ) {
			
			// create new methodNameList
			List<String> newMethodNameList = new ArrayList<>();
			// add methodName to methodNameList
			newMethodNameList.add(methodName);
			// add methodTime and methodNameList to methodTimeMap
			methodTimeMap.put(methodTime, newMethodNameList);
			
		}
		
		else {
			
			// add methodName to methodNameList
			existingMethodNameList.add(methodName);
			// add methodTime and methodNameList to methodTimeMap
			methodTimeMap.put(methodTime, existingMethodNameList);
			
		}
		
	}
	
	public List<String> get( long methodTime ) {
		
		return methodTimeMap.get(methodTime);
		
	}
	
	public int size( ) {
		
		return methodTimeMap.size();
		
	}
	
	public NavigableSet<Long> descendingKeySet( ) {
		
		return methodTimeMap.descendingKeySet();
		
	}
	
	public String toString( ) {
		
		return methodTimeMap.toString();
		
	}
	
	public NavigableMap<Long, List<String>> getMethodTimeMap() {
		
		return methodTimeMap;
		
	}

}
