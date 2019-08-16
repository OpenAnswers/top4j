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
