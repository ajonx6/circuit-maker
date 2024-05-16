package circuit.input;

import java.awt.event.*;

// Allows mouse input (both just to check a state change or the actual state)
public class MouseInput implements MouseListener, MouseMotionListener, MouseWheelListener {
	public static final int NUM_BUTTONS = 10;
	public static final boolean[] BUTTONS = new boolean[NUM_BUTTONS];
	public static final boolean[] LAST_BUTTONS = new boolean[NUM_BUTTONS];

	public static int x = -1, y = -1;
	public static int lastX = x, lastY = y;
	public static boolean moving = false;
	public static double secondsNotMoving = 0;
	public static int mouseWheelDir = 0;

	public void mousePressed(MouseEvent e) {
		x = e.getX();
		y = e.getY();
		BUTTONS[e.getButton()] = true;
	}

	public void mouseReleased(MouseEvent e) {
		x = e.getX();
		y = e.getY();
		BUTTONS[e.getButton()] = false;
	}

	public void mouseDragged(MouseEvent e) {
		x = e.getX();
		y = e.getY();
	}

	public void mouseMoved(MouseEvent e) {
		x = e.getX();
		y = e.getY();
		moving = true;
	}

	public static void tick(double delta) {
		System.arraycopy(BUTTONS, 0, LAST_BUTTONS, 0, NUM_BUTTONS);

		if (x == lastX && y == lastY) moving = false;
		lastX = x;
		lastY = y;

		if (moving) secondsNotMoving = 0;
		else secondsNotMoving += delta;
	}

	public static boolean isPressed(int button) {
		return BUTTONS[button];
	}

	public static boolean wasPressed(int button) {
		return isPressed(button) && !LAST_BUTTONS[button];
	}

	public static boolean wasReleased(int button) {
		return !isPressed(button) && LAST_BUTTONS[button];
	}

	public static boolean notMovedFor(double seconds) {
		return secondsNotMoving >= seconds;
	}

	public static boolean isMoving() {
		return moving;
	}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mouseWheelMoved(MouseWheelEvent e) {
		// Game.getInstance().editor.xOffset += e.getWheelRotation() * 10;
	}
}