package circuit.util;

public class Timer {
	public int timer;
	public int ticks = 0;

	public Timer(int timer) {
		this.timer = timer;
	}

	public boolean tick() {
		if (ticks >= timer) {
			ticks = 0;
			return true;
		}
		ticks++;
		return false;
	}

	public double percent() {
		return (double) ticks / (double) timer;
	}
}