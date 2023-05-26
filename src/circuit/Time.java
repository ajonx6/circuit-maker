package circuit;

public class Time {
	public static final long SECOND = 1000000000L;

	public static double delta;

	public static long getTime() {
		return System.nanoTime();
	}

	public static double getFrameTimeInSeconds() {
		return delta;
	}
}