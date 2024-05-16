package circuit.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

// Allows keyboard input (both just to check a state change or the actual state)
public class KeyInput extends KeyAdapter {
	public static final int NUM_KEYS = 256;
	// Value of the key presses this frame
	public static final boolean[] KEYS = new boolean[NUM_KEYS];
	// Value of the key presses last frame
	public static final boolean[] LAST_KEYS = new boolean[NUM_KEYS];

	public void keyPressed(KeyEvent e) {
		KEYS[e.getKeyCode()] = true;
	}

	public void keyReleased(KeyEvent e) {
		KEYS[e.getKeyCode()] = false;
	}

	public static void tick() {
		System.arraycopy(KEYS, 0, LAST_KEYS, 0, NUM_KEYS);
	}

	public static boolean isDown(int key) {
		return KEYS[key];
	}

	public static boolean wasPressed(int key) {
		return isDown(key) && !LAST_KEYS[key];
	}

	public static boolean wasReleased(int key) {
		return !isDown(key) && LAST_KEYS[key];
	}
}