package circuit.graphics;

import circuit.Game;
import circuit.components.Circuit;
import circuit.components.Pin;
import circuit.components.Wire;
import circuit.input.MouseInput;
import circuit.util.Util;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Editor {
    public static final int PIN_RADIUS = 5;
    public static final int IOPIN_GAP = 15;

    public Circuit currentCircuit = new Circuit("current");

    public List<PinGraphic> pins = new ArrayList<>();
    public List<WireGraphic> wires = new ArrayList<>();
    public PinGraphic startDrawLine = null;

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

    public void event(int button, String type) {
        if (type.equals("CLICK")) {
            if (MouseInput.y <= Game.HEIGHT - Game.CIRCUIT_LIST_HEIGHT) {
                if (button == MouseEvent.BUTTON1) {
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
                } else if (button == MouseEvent.BUTTON3) {
                    if (startDrawLine != null) {
                        startDrawLine = null;
                    }
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
        }
        currentCircuit.tick();
    }


    public void render(Renderer renderer) {
        renderer.drawCircuit("nand", 100, 100, 70, 39, 0xffff00ff, 0xffffffff);
        renderer.drawPin(100, 113, PIN_RADIUS, 0);
        renderer.drawPin(100, 126, PIN_RADIUS, 0);
        renderer.drawPin(169, 120, PIN_RADIUS, 0);

        renderer.drawCircuit("nand", 100, 100, 70, 39, 0xffff00ff, 0xffffffff);
        renderer.drawPin(100, 113, PIN_RADIUS, 0);
        renderer.drawPin(100, 126, PIN_RADIUS, 0);
        renderer.drawPin(169, 120, PIN_RADIUS, 0);

        for (PinGraphic p : pins) {
            renderer.drawPin(p.getX(), p.getY(), PIN_RADIUS, p.getPin().getState() ? 0xff0000 : 0);
        }

        for (WireGraphic w : wires) {
            renderer.drawWire(w.getX1(), w.getY1(), w.getX2(), w.getY2(), currentCircuit.getPinByID(w.getWire().getPid1()).getState() ? 0xff0000 : 0);
        }

        if (startDrawLine != null) {
            renderer.drawWire(startDrawLine.getX(), startDrawLine.getY(), MouseInput.x, MouseInput.y, startDrawLine.getPin().getState() ? 0xff0000 : 0);
        }
    }
}