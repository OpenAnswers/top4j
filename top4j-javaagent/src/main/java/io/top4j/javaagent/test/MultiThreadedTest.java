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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class MultiThreadedTest {

	private int NUM_THREADS;
	private int NUM_ITERATIONS;
	private int PAUSE_TIME;

	private static final Logger LOGGER = Logger.getLogger(MultiThreadedTest.class.getName());

	public MultiThreadedTest(int numThreads, int numIterations, int pauseTime) {

		this.NUM_THREADS = numThreads;
		this.NUM_ITERATIONS = numIterations;
		this.PAUSE_TIME = pauseTime;

	}

	public static void main(String[] args)
	{
		int numThreads = 1;
		int numIterations = 1;
		int pauseTime = 1;
		LOGGER.info("Running Top4J Multithreaded Test....");
		if (args.length == 0) {
			LOGGER.severe("USAGE: MultiThreadedTest <num-threads> <num-iterations> <pause-time>");
			System.exit(1);
		}
		else {
			try {
				numThreads = Integer.parseInt(args[0]);
				numIterations = Integer.parseInt(args[1]);
				pauseTime = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				LOGGER.severe("Argument must be an integer");
			}
		}
		LOGGER.info("Number of threads = " + numThreads);
		LOGGER.info("Number of iterations = " + numIterations);
		LOGGER.info("Pause time between iterations = " + pauseTime + " ms");
		MultiThreadedTest multiThreadedTest = new MultiThreadedTest(numThreads, numIterations, pauseTime);
		multiThreadedTest.run();
	}

	public void run()
	{
		long start = System.currentTimeMillis();

		Thread[] t = new Thread[NUM_THREADS];

		for(int i=0; i< NUM_THREADS; i++)
		{
			t[i] = new Thread(new CPUBurner(NUM_ITERATIONS, PAUSE_TIME), "CPUBurner-" + i);
			t[i].start();
		}

		for(int i=0; i< NUM_THREADS; i++)
		{
			try
			{
				t[i].join();
			}
			catch (InterruptedException ie)
			{
				Thread.currentThread().interrupt();
			}
		}

		long end = System.currentTimeMillis();

		double seconds = (end - start) / 1000.0;

		LOGGER.info("Calculations              : " + NUM_THREADS * NUM_ITERATIONS);
		LOGGER.info("Duration                  : " + seconds);
		LOGGER.info("TPS                       : " + NUM_THREADS * NUM_ITERATIONS / seconds);
	}

	private class CPUBurner implements Runnable
	{
		private final int numIterations;
		private final int maxPauseTime;
		private final long n = 20;
		private Random randomGenerator = new Random();
		private Map<Integer, Long> result = new HashMap<>();

		public CPUBurner(int numIterations, int maxPauseTime)
		{
			this.numIterations = numIterations;
			this.maxPauseTime = maxPauseTime;
		}

		public void run() {

			for(int i=1; i<=numIterations; i++)
			{
				if (i % 100 == 0)
				{
					LOGGER.fine(Thread.currentThread().getName() + " : " + i);
				}

				// generate randomPause between 0 and maxPauseTime
				int randomPause = randomGenerator.nextInt(maxPauseTime);

				// do some work
				doWork( randomPause );

			}

		}
		
		private void doWork( int randomPause ){

			synchronized (CPUBurner.class) {
				// calculate the nth fibonacci number
				long fibonacci = fibonacci(n);
				LOGGER.fine("The " + n + "th Fibonacci number is calculated as " + fibonacci);
				// then sleep for randomPause milliseconds to generate some thread contention
				try {
					Thread.sleep(randomPause);
				} catch (InterruptedException ie) {
					LOGGER.severe("Thread sleep interrupted.");
				}

			}
			
		}

        private long fibonacci(long N) {

            long f = 0, g = 1;
            String fibSeq = null;

            for (int i = 1; i <= N; i++) {
                f = f + g;
                g = f - g;
                fibSeq = fibSeq + " " + f;
				// store result
				result.put(i, f);
            }

            LOGGER.fine("Fibonacci Sequence: " + fibSeq);
            return f;

        }

	}

}
