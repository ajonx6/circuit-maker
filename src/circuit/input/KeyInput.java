package circuit.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyInput extends KeyAdapter {
	public static final int NUM_KEYS = 256;
	public static final boolean[] KEYS = new boolean[NUM_KEYS];
	public static final boolean[] LAST_KEYS = new boolean[NUM_KEYS];

	public void keyPressed(KeyEvent e) {
		KEYS[e.getKeyCode()] = true;
	}

	public void keyReleased(KeyEvent e) {
		KEYS[e.getKeyCode()] = false;
	}

	public static void tick() {
		for (int i = 0; i < NUM_KEYS; i++) {
			LAST_KEYS[i] = KEYS[i];
		}
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