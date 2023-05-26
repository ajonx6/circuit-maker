package circuit;

import circuit.components.*;
import circuit.graphics.Editor;
import circuit.graphics.Renderer;
import circuit.input.KeyInput;
import circuit.input.MouseInput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.util.Scanner;

public class Game extends Canvas implements Runnable {
    private static final long serialVersionUID = 1L;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int CIRCUIT_LIST_HEIGHT = 110;
    public static final String TITLE = "Circuit Visualiser";
    public static final double FPS = 600.0;

    private static Game instance;
    private static boolean pause = false;

    public JFrame frame;
    public boolean running;
    public Renderer renderer;
    public Editor editor;

    private Game() {
        renderer = Renderer.getInstance();
        editor = new Editor();
        editor.initIOPins(2, 3);
        
        Circuit.loadAllCircuits();
        // Circuit fulladder = Circuit.CIRCUITS.get("fulladder").copy();
        // fulladder.print();
        //
        // Scanner scanner = new Scanner(System.in);
        // while (true) {
        //     String line = scanner.nextLine();
        //     if (line.equals("done")) break;
        //     int pinID = Integer.parseInt(line);
        //     fulladder.getInputPins().get(pinID).setState(!fulladder.getInputPins().get(pinID).getState());
        //     fulladder.tick();
        //     System.out.println("\n\n");
        //     fulladder.print();
        // }
        
        KeyInput ki = new KeyInput();
        MouseInput mi = new MouseInput();
        addKeyListener(ki);
        addMouseListener(mi);
        addMouseMotionListener(mi);
        addMouseWheelListener(mi);
    }

    public static Game getInstance() {
        if (instance == null) instance = new Game();
        return instance;
    }

    public void stop() {
        if (!running) return;
        running = false;
    }

    public void run() {
        running = true;
        requestFocus();

        int frames = 0, ticks = 0;
        long frameCounter = 0;
        double frameTime = 1.0 / FPS;
        long lastTime = Time.getTime();
        double unprocessedTime = 0;

        while (running) {
            boolean render = false;

            long startTime = Time.getTime();
            long passedTime = startTime - lastTime;
            lastTime = startTime;

            unprocessedTime += passedTime / (double) Time.SECOND;
            frameCounter += passedTime;

            while (unprocessedTime > frameTime) {
                render = true;
                unprocessedTime -= frameTime;
                Time.delta = frameTime;
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
        
        if (MouseInput.wasPressed(MouseEvent.BUTTON1)) {
            editor.event(MouseEvent.BUTTON1, "CLICK");
        }
        if (MouseInput.wasPressed(MouseEvent.BUTTON2)) {
            editor.event(MouseEvent.BUTTON2, "CLICK");
        }
        if (MouseInput.wasPressed(MouseEvent.BUTTON3)) {
            editor.event(MouseEvent.BUTTON3, "CLICK");
        }
        if (MouseInput.isPressed(MouseEvent.BUTTON3)) {
            editor.event(MouseEvent.BUTTON3, "PRESS");
        }

        KeyInput.tick();
        MouseInput.tick(Time.getFrameTimeInSeconds());
    }

    public void render() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(2);
            return;
        }
        renderer.setGraphics(bs);
        renderer.fillRect(0, 0, WIDTH, HEIGHT - CIRCUIT_LIST_HEIGHT, 0x555555);
        renderer.fillRect(0, HEIGHT - CIRCUIT_LIST_HEIGHT, WIDTH, HEIGHT, 0x777777);
        
        editor.render(renderer);

        renderer.finish();
    }
}