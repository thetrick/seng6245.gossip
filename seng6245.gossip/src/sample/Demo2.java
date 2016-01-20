package sample;

public class Demo2 {
	public int divide(int dividend, int divisor) {
		// the following is just an example
		if (divisor == 0) {
			throw new IllegalArgumentException("Cannot divide by zero.");
		}
		return dividend / divisor;
	}
}
