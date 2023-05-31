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
import java.io.Serial;

public class Game extends Canvas implements Runnable {
    @Serial private static final long serialVersionUID = 1L;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int CIRCUIT_LIST_HEIGHT = 70;
    public static final String TITLE = "Circuit Visualiser";
    public static final double FPS = 600.0;

    private static Game instance;

    public JFrame frame;
    public boolean running;
    public Renderer renderer;
    public Editor editor;

    private Game() {
        Circuit.loadAllCircuits();
        renderer = Renderer.getInstance();
        editor = new Editor();
        editor.init(2, 1);
        
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
        double delta = Time.getFrameTimeInSeconds();
        
        if (MouseInput.wasPressed(MouseEvent.BUTTON1)) {
            editor.event(MouseEvent.BUTTON1, "CLICK", delta);
        }
        if (MouseInput.wasPressed(MouseEvent.BUTTON2)) {
            editor.event(MouseEvent.BUTTON2, "CLICK", delta);
        }
        if (MouseInput.wasPressed(MouseEvent.BUTTON3)) {
            editor.event(MouseEvent.BUTTON3, "CLICK", delta);
        }
        if (MouseInput.isPressed(MouseEvent.BUTTON3)) {
            editor.event(MouseEvent.BUTTON3, "PRESS", delta);
        }
        
        /* Prints the truth table for the circuit
        // if (KeyInput.wasPressed(KeyEvent.VK_T)) {
        //     for (Pin p : editor.currentCircuit.getInputPins()) {
        //         p.setState(true);
        //     }
        //     editor.currentCircuit.tick();
        //    
        //     StringBuilder sb = new StringBuilder();
        //     StringBuilder top = new StringBuilder();
        //     for (int i = 0; i < editor.currentCircuit.getInputPins().size(); i++) {
        //         top.append("I_").append(i).append("|");
        //     }
        //     top.append("|");
        //     for (int i = 0; i < editor.currentCircuit.getOutputPins().size(); i++) {
        //         top.append("|O_").append(i);
        //     }
        //     top.append("\n");
        //     sb.append(top);
        //
        //     sb.append("=".repeat(Math.max(0, top.length() - 1)));
        //     sb.append("\n");
        //    
        //     for (int i = 0; i < (int) Math.pow(2, editor.currentCircuit.getInputPins().size()); i++) {
        //         editor.increaseInputsByOne();
        //         StringBuilder toAdd = new StringBuilder();
        //         for (Pin p : editor.currentCircuit.getInputPins()) {
        //             toAdd.append(" ").append(p.getState() ? 1 : 0).append(" |");
        //         }
        //         toAdd.append("|");
        //         for (Pin p : editor.currentCircuit.getOutputPins()) {
        //             toAdd.append("| ").append(p.getState() ? 1 : 0).append(" ");
        //         }
        //         sb.append(toAdd).append("\n");
        //     }
        //     editor.increaseInputsByOne();
        //     System.out.println(sb);
        // }*/ 
                
        // Increase inputs by 0b1
        if (KeyInput.wasPressed(KeyEvent.VK_P)) {
            editor.increaseInputsByOne(delta);
        }

        // Save component
        if (KeyInput.wasPressed(KeyEvent.VK_S)) {
            String name = JOptionPane.showInputDialog("Circuit Name:");
            Color compCol = JColorChooser.showDialog(frame, "Choose Circuit Colour", Color.WHITE);
            Color textCol = JColorChooser.showDialog(frame, "Choose Circuit Text Colour", Color.WHITE);
            
            for (Pin p : editor.currentCircuit.getInputPins()) {
                p.setState(false);
            }
            editor.currentCircuit.tick(delta);
            
            editor.currentCircuit.setName(name);
            editor.currentCircuit.setWidth(Math.max(renderer.g.getFontMetrics().stringWidth(name) * 2, 40));
            editor.currentCircuit.setHeight(Editor.IOPIN_GAP * Math.max(editor.currentCircuit.getInputPins().size(), editor.currentCircuit.getOutputPins().size()));
            editor.currentCircuit.setCircuitColor(compCol.getRGB() & 0xffffff);
            editor.currentCircuit.setTextColor(textCol.getRGB() & 0xffffff);

            editor.currentCircuit.save();
            Circuit.CIRCUITS.put(name, editor.currentCircuit);
            
            editor.newEditor();
            
            // CircuitList.circuitList.add(editor.currentCircuit);
            // for (Pin p : editor.currentCircuit.pins.values()) {
            //     p.set(false);
            // }
            // editor.currentCircuit.calculateHeight();
            // editor.newEditor();
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
        renderer.fillRect(0, 0, WIDTH, HEIGHT - CIRCUIT_LIST_HEIGHT, 0x555555);
        renderer.fillRect(0, HEIGHT - CIRCUIT_LIST_HEIGHT, WIDTH, HEIGHT, 0x777777);
        
        editor.generateCircuitListPositions();
        editor.render(renderer);

        renderer.finish();
    }
}