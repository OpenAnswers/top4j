package io.top4j.javaagent.test;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class CollatorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		log("Running Collator Test....");
		NavigableMap<Long, Integer> sortedMap = new TreeMap<Long, Integer>();
		Random randomGenerator = new Random();
		
		for (int idx = 1; idx <= 10; ++idx){
		      int randomInt = randomGenerator.nextInt(100000);
		      long randomLong = randomGenerator.nextLong();
		      log("Generated : " + randomLong + " -> " + randomInt);
		      sortedMap.put(randomLong, randomInt);
		}
		
		for (Long key : sortedMap.descendingKeySet()) {
			log(String.valueOf(key) + " -> " + sortedMap.get(key));
		}

	}
	
	private static void log(String aMessage){
	    System.out.println(aMessage);
	}

}
