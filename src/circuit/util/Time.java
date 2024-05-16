package circuit.util;

public class Time {
	public static final long SECOND = 1000000000L;
	
	// Represents the time taken for the current tick (not render)
	private static double delta;
	
	public static void setFrameTime(double delta) {
		Time.delta = delta;
	}

	public static double getFrameTime() {
		return delta;
	}
}