package circuit;

import circuit.components.*;
import circuit.graphics.Editor;
import circuit.graphics.Renderer;
import circuit.input.KeyInput;
import circuit.input.MouseInput;
import circuit.util.Time;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.io.Serial;

public class ProgramManager extends Canvas implements Runnable {
	@Serial
	private static final long serialVersionUID = 1L;

	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	public static final int CIRCUIT_LIST_HEIGHT = 70;
	public static final String TITLE = "Circuit Visualiser";
	// public static final double UPS = 500.0;
	public static final double FPS = 600.0;

	public JFrame frame;
	public Thread thread;
	public boolean running;

	public Renderer renderer;
	public Editor editor;

	public ProgramManager() {
		Circuit.loadAllCircuits();
		renderer = Renderer.getInstance();
		editor = new Editor();
		editor.initialiseNewCircuit(2, 1);

		KeyInput ki = new KeyInput();
		MouseInput mi = new MouseInput();
		addKeyListener(ki);
		addMouseListener(mi);
		addMouseMotionListener(mi);
		addMouseWheelListener(mi);
	}

	public void start() {
		if (running) return;
		thread = new Thread(this, "Circuit");
		thread.start();
	}

	public void stop() {
		if (!running) return;
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	// Keeps the programming running at the specific tps and fps
	public void run() {
		running = true;
		requestFocus();

		int frames = 0, ticks = 0;
		long frameCounter = 0;
		double frameTime = 1.0 / FPS;
		long lastTime = System.nanoTime();
		double unprocessedTime = 0;

		while (running) {
			boolean render = false;

			long startTime = System.nanoTime();
			long passedTime = startTime - lastTime;
			lastTime = startTime;

			unprocessedTime += passedTime / (double) Time.SECOND;
			frameCounter += passedTime;

			while (unprocessedTime > frameTime) {
				render = true;
				unprocessedTime -= frameTime;
				Time.setFrameTime(frameTime);
				tick();
				ticks++;
				if (frameCounter >= Time.SECOND) {
					frame.setTitle(TITLE + " | FPS: " + frames + ", UPS: " + ticks);
					frames = 0;
					ticks = 0;
					frameCounter = 0;
				}
			}
			if (render) {
				render();
				frames++;
			} else {
				try {
					Thread.sleep(1);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
	}

	public void tick() {
		if (KeyInput.wasPressed(KeyEvent.VK_ESCAPE)) System.exit(0);
		double delta = Time.getFrameTime();

		// Pickup/place circuit, create/end wire
		if (MouseInput.wasPressed(MouseEvent.BUTTON1)) {
			editor.event(MouseEvent.BUTTON1, "CLICK", delta);
		}
		// Currently unused
		if (MouseInput.wasPressed(MouseEvent.BUTTON2)) {
			editor.event(MouseEvent.BUTTON2, "CLICK", delta);
		}
		// Delete a component
		if (MouseInput.wasPressed(MouseEvent.BUTTON3)) {
			editor.event(MouseEvent.BUTTON3, "CLICK", delta);
		}
		// Currently unused
		if (MouseInput.isPressed(MouseEvent.BUTTON3)) {
			editor.event(MouseEvent.BUTTON3, "PRESS", delta);
		}

		// Increase inputs by 0b1
		if (KeyInput.wasPressed(KeyEvent.VK_P)) {
			editor.increaseInputsByOne(delta);
		}

		// Save component
		if (KeyInput.wasPressed(KeyEvent.VK_S)) {
			UIWindows.saveCircuitUI(this);
		}

		// New editor
		if (KeyInput.wasPressed(KeyEvent.VK_N)) {
			editor.newEditor();
		}
		
		editor.currentCircuit.tick(delta);
		KeyInput.tick();
		MouseInput.tick(delta);
	}

	public void render() {
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(2);
			return;
		}

		renderer.setGraphics(bs);

		// Renders the main background color
		renderer.fillRect(0, 0, WIDTH, HEIGHT - CIRCUIT_LIST_HEIGHT, 0x555555);
		// Renders a box to display the potential circuits
		renderer.fillRect(0, HEIGHT - CIRCUIT_LIST_HEIGHT, WIDTH, HEIGHT, 0x777777);
		// Renders the circuits in the correct positions in the circuit list 
		editor.generateCircuitListPositions();
		// Draws all the components
		editor.render(renderer);

		renderer.finish();
	}
}