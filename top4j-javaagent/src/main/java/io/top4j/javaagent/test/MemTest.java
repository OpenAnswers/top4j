package io.top4j.javaagent.test;

public class MemTest {

	static String DATA = "THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!! THIS IS A TEST STRING!!";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println( "Firing up JavaAgent memory test...." );
		
		int iterations = 30000;
		
		for (int i = 0; i < iterations; i++) {
			
			churnMemory();
				
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				System.err.println("Thread sleep interrupted.");
			}
			
		}

	}
	
	public static void churnMemory ( ) {
		
		StringBuilder sb = new StringBuilder();
		int iterations = 100;
		
		for (int i = 0; i < iterations; i++) {
			
			sb.append(DATA);
			
		}
		
	}

}
