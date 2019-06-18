package io.top4j.javaagent.test;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println( "Firing up JavaAgent test...." );
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			System.err.println("Thread sleep interrupted.");
		}

	}

}
