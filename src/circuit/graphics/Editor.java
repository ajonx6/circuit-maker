package circuit.graphics;

import circuit.Game;
import circuit.components.Circuit;
import circuit.components.Pin;
import circuit.components.Wire;
import circuit.input.MouseInput;
import circuit.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Editor {
    public static final int CIRCUIT_WIDTH_PADDING = 8;
    public static final int PIN_RADIUS = 5;
    public static final int IOPIN_GAP = 18;

    public Circuit currentCircuit;

    public List<PinGraphic> pins = new ArrayList<>();
    public List<WireGraphic> wires = new ArrayList<>();
    public List<CircuitGraphic> circuits = new ArrayList<>();
    public PinGraphic startDrawLine = null;
    public CircuitGraphic currentlyHeldCircuit = null;

    public int xOffset = 0;
    public List<CircuitGraphic> circuitList = new ArrayList<>();

    public void init(int i, int o) {
        currentCircuit = new Circuit("current", 0, 0, 0, 0);
        initIOPins(i, o);
    }
    
    public void initIOPins(int ins, int outs) {
        double startY;
        if (ins % 2 == 0) startY = (Game.HEIGHT - Game.CIRCUIT_LIST_HEIGHT) / 2.0 - (ins / 2 - 0.5) * IOPIN_GAP;
        else startY = (Game.HEIGHT - Game.CIRCUIT_LIST_HEIGHT) / 2.0 - ins / 2 * IOPIN_GAP;
        for (int i = 0; i < ins; i++) {
            Pin p = currentCircuit.addPin(Pin.INPUT);
            pins.add(new PinGraphic(p, (int) (PIN_RADIUS * 1.5), (int) (startY + i * IOPIN_GAP)));
        }

        if (outs % 2 == 0) startY = (Game.HEIGHT - Game.CIRCUIT_LIST_HEIGHT) / 2.0 - (outs / 2 - 0.5) * IOPIN_GAP;
        else startY = (Game.HEIGHT - Game.CIRCUIT_LIST_HEIGHT) / 2.0 - outs / 2 * IOPIN_GAP;
        for (int i = 0; i < outs; i++) {
            Pin p = currentCircuit.addPin(Pin.OUTPUT);
            pins.add(new PinGraphic(p, Game.WIDTH - (int) (PIN_RADIUS * 1.5), (int) (startY + i * IOPIN_GAP)));
        }
    }
    
    public List<PinGraphic> generateSelectedCircuitPinGraphics(CircuitGraphic cg) {
        List<PinGraphic> ps = new ArrayList<>();
        
        int numIn = cg.getCircuit().getInputPins().size();
        int gap = IOPIN_GAP; // cg.getCircuit().getHeight() / (numIn + 1);
        double startY;
        if (numIn % 2 == 0) startY = cg.getY() + cg.getCircuit().getHeight() / 2 - (numIn / 2 - 0.5) * gap;
        else startY = cg.getY() + cg.getCircuit().getHeight() / 2 - (numIn / 2) * gap;
        for (int i = 0; i < numIn; i++) {
            PinGraphic pg = new PinGraphic(cg.getCircuit().getInputPins().get(i), cg.getX(), (int) (startY + i * gap));
            ps.add(pg);
            // System.out.println("PINS: (" + cg.getCircuit().getName() + ") " + pg.getPin().getIds() + " / " + numIn);
        }
        // System.out.println("DONE");

        
        int numOut = cg.getCircuit().getOutputPins().size();
        gap = IOPIN_GAP; // cg.getCircuit().getHeight() / (numOut + 1);
        if (numOut % 2 == 0) startY = cg.getY() + cg.getCircuit().getHeight() / 2 - (numOut / 2 - 0.5) * gap;
        else startY = cg.getY() + cg.getCircuit().getHeight() / 2 - (numOut / 2) * gap;
        for (int i = 0; i < numOut; i++) {
            PinGraphic pg = new PinGraphic(cg.getCircuit().getOutputPins().get(i), cg.getX() + cg.getCircuit().getWidth(), (int) (startY + i * gap));
            ps.add(pg);
        }
        
        return ps;
    }

    public void generateCircuitListPositions(Renderer renderer) {
        int xo = CIRCUIT_WIDTH_PADDING;
        for (Circuit c : Circuit.CIRCUITS.values()) {
            // int width = renderer.g.getFontMetrics().stringWidth(c.getName()) * 2;
            int width = Math.max(c.getWidth(), 42);
            CircuitGraphic cg = new CircuitGraphic(c, xo + xOffset, Game.HEIGHT - Game.CIRCUIT_LIST_HEIGHT / 2 - 20, width, 40);
            circuitList.add(cg);
            xo += width + CIRCUIT_WIDTH_PADDING;
        }
    }

    public void event(int button, String type) {
        if (type.equals("CLICK")) {
            if (MouseInput.y <= Game.HEIGHT - Game.CIRCUIT_LIST_HEIGHT) {
                if (button == MouseEvent.BUTTON1) {
                    if (currentlyHeldCircuit != null) placeCircuit();
                    else drawWireEvent();
                }
                else if (button == MouseEvent.BUTTON3) deletePinWireEvent();
            } else {
                if (startDrawLine != null) return;
                else if (button == MouseEvent.BUTTON1) selectCircuitEvent();
                else if (button == MouseEvent.BUTTON3) cancelAllSelected();
            }
        }
        currentCircuit.tick();
    }
    
    public void placeCircuit() {
        currentlyHeldCircuit.setX(MouseInput.x - currentlyHeldCircuit.getCircuit().getWidth() / 2);
        currentlyHeldCircuit.setY(MouseInput.y - currentlyHeldCircuit.getCircuit().getHeight() / 2);
        // circuits.add(currentlyHeldCircuit);
        // int x = currentCircuit.pins.size();
        Circuit newCircuit = currentCircuit.addCircuit(currentlyHeldCircuit.getCircuit());
        // currentCircuit.generateDelays();
        // generatePinsForCircuit(currentlyHeldCircuit, pins, x);
        // currentlyHeldPins.clear();
        CircuitGraphic cg = new CircuitGraphic(newCircuit, MouseInput.x - currentlyHeldCircuit.getCircuit().getWidth() / 2, MouseInput.y - currentlyHeldCircuit.getCircuit().getHeight() / 2, newCircuit.getWidth(), newCircuit.getHeight());
        List<PinGraphic> ps = generateSelectedCircuitPinGraphics(cg);
        // System.out.println("PLACING CIRCUIT");
        // for (PinGraphic p : ps) {
        //     System.out.println("PINPINPIN: " + p.getPin().getIds());
        // }
        pins.addAll(ps);
        circuits.add(cg);
        currentlyHeldCircuit = null;
    }
    
    public void drawWireEvent() {
        boolean drawing = startDrawLine != null;
        for (PinGraphic pg : pins) {
            double dist = Util.distanceBetweenPoints(pg.getX(), pg.getY(), MouseInput.x, MouseInput.y);
            if (dist < 5) {
                if (startDrawLine == null) {
                    startDrawLine = pg;
                } else if (startDrawLine == pg) {
                    startDrawLine = null;
                } else {
                    Wire wire = currentCircuit.addWire(startDrawLine.getPin().getIds(), pg.getPin().getIds());
                    WireGraphic wg = new WireGraphic(wire, startDrawLine.getX(), startDrawLine.getY(), pg.getX(), pg.getY());
                    wires.add(wg);
                    startDrawLine = null;
                }
                break;
            }
        }
        if (drawing && startDrawLine != null) {
            Pin p = currentCircuit.addPin(Pin.NONE);
            PinGraphic pg = new PinGraphic(p, MouseInput.x, MouseInput.y);
            pins.add(pg);
            Wire wire = currentCircuit.addWire(startDrawLine.getPin().getIds(), pg.getPin().getIds());
            WireGraphic wg = new WireGraphic(wire, startDrawLine.getX(), startDrawLine.getY(), pg.getX(), pg.getY());
            wires.add(wg);
            startDrawLine = pg;
        }
    }
    
    public void deletePinWireEvent() {
        if (startDrawLine != null || currentlyHeldCircuit != null) cancelAllSelected();
        else {
            for (PinGraphic pg : pins) {
                if (Util.distanceBetweenPoints(pg.getX(), pg.getY(), MouseInput.x, MouseInput.y) < PIN_RADIUS) {
                    List<Wire> wiresToDelete = currentCircuit.interactWithPin(pg.getPin());
                    if (pg.getPin().getType() == Pin.NONE) {
                        pins.remove(pg);
                        Iterator<WireGraphic> it = wires.iterator();
                        while (it.hasNext()) {
                            WireGraphic wg = it.next();
                            if (wiresToDelete.contains(wg.getWire())) it.remove();
                        }
                    }
                    return;
                }
            }

            for (WireGraphic wg : wires) {
                if (Util.distanceFromLine(wg.getX1(), wg.getY1(), wg.getX2(), wg.getY2(), MouseInput.x, MouseInput.y) < Renderer.WIRE_WIDTH) {
                    currentCircuit.removeWire(wg.getWire());
                    wires.remove(wg);
                    break;
                }
            }
        }
    }
    
    public void selectCircuitEvent() {
        // if (currentlyHeldSSD == null) {
        for (CircuitGraphic cg : circuitList) {
            if (Util.isPointWithinBox(MouseInput.x, MouseInput.y, cg.getX() - CIRCUIT_WIDTH_PADDING + xOffset, cg.getY() - CIRCUIT_WIDTH_PADDING, cg.getWidth() + 2 * CIRCUIT_WIDTH_PADDING, cg.getHeight() + 2 * CIRCUIT_WIDTH_PADDING)) {
                currentlyHeldCircuit = cg.copy();
                return;
            }
        }
        // }
        // if (currentlyHeldCircuit == null && isPointWithinBox(MouseInput.x, MouseInput.y, listSSD.x - PADDING + xOffset, listSSD.y - PADDING, listSSD.ssd.getWidth() + 2 * PADDING, listSSD.ssd.getHeight() + 2 * PADDING)) {
        //     currentlyHeldSSD = listSSD.copy();
        // }
    }
    
    public void cancelAllSelected() {
        startDrawLine = null;
        currentlyHeldCircuit = null;
    }

    public void increaseInputsByOne() {
        int currentInput = 0;
        for (int i = currentCircuit.getInputPins().size() - 1; i >= 0; i--) {
            currentInput |= currentCircuit.getInputPins().get(i).getState() ? 1 : 0;
            currentInput <<= 1;
        }
        currentInput >>= 1;
        currentInput++;
        for (int i = 0; i < currentCircuit.getInputPins().size(); i++) {
            currentCircuit.getInputPins().get(i).setState((currentInput & 0b1) == 1);
            currentInput >>= 1;
        }
        currentCircuit.tick();
    }

    public void render(Renderer renderer) {
        for (CircuitGraphic c : circuits) {
            renderer.drawCircuit(c.getCircuit().getName(), c.getX(), c.getY(), c.getCircuit().getWidth(), c.getCircuit().getHeight(), c.getCircuit().getCircuitColor(), c.getCircuit().getTextColor());
        }
        
        for (PinGraphic p : pins) {
            renderer.drawPin(p.getX(), p.getY(), PIN_RADIUS, p.getPin().getState() ? 0xff0000 : 0);
        }

        for (WireGraphic w : wires) {
            renderer.drawWire(w.getX1(), w.getY1(), w.getX2(), w.getY2(), currentCircuit.getPinByID(w.getWire().getPid1()).getState() ? 0xff0000 : 0);
        }

        if (startDrawLine != null) {
            renderer.drawWire(startDrawLine.getX(), startDrawLine.getY(), MouseInput.x, MouseInput.y, startDrawLine.getPin().getState() ? 0xff0000 : 0);
        }

        if (currentlyHeldCircuit != null) {
            currentlyHeldCircuit.setX(MouseInput.x - currentlyHeldCircuit.getCircuit().getWidth() / 2);
            currentlyHeldCircuit.setY(MouseInput.y - currentlyHeldCircuit.getCircuit().getHeight() / 2);
            renderer.drawCircuit(currentlyHeldCircuit.getCircuit().getName(), currentlyHeldCircuit.getX(), currentlyHeldCircuit.getY(), currentlyHeldCircuit.getCircuit().getWidth(), currentlyHeldCircuit.getCircuit().getHeight(), currentlyHeldCircuit.getCircuit().getCircuitColor(), currentlyHeldCircuit.getCircuit().getTextColor());
            List<PinGraphic> ps = generateSelectedCircuitPinGraphics(currentlyHeldCircuit);
            for (PinGraphic pg : ps) {
                renderer.drawPin(pg.getX(), pg.getY(), PIN_RADIUS, 0);
            }
        }


        for (CircuitGraphic cg : circuitList) {
            renderer.drawCircuit(cg.getCircuit().getName(), cg.getX(), cg.getY(), cg.getCircuit().getWidth(), cg.getHeight(), cg.getCircuit().getCircuitColor(), cg.getCircuit().getTextColor());
        }
    }

    public void newEditor() {
        String res = JOptionPane.showInputDialog("Enter number of inputs and outputs (i o)");
        if (res == null) return;
        pins.clear();
        wires.clear();
        circuits.clear();
        circuitList.clear();
        xOffset = 0;
        currentlyHeldCircuit = null;
        startDrawLine = null;
        String[] tokens = res.split(" ");
        init(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
    }
}