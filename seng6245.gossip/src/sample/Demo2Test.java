package sample;

import static org.junit.Assert.*;

import org.junit.Test;

public class Demo2Test extends Demo2 {

	@Test(expected = IllegalArgumentException.class)
	public void testExceptionIsThrown() {
		Demo2 tester = new Demo2();
		tester.divide(100, 0);
	}
	
	@Test
	public void testDivide(){
		Demo2 tester = new Demo2();
		assertEquals("100 divided by 10 must be 10.", 10, tester.divide(100, 10));
	}

}
