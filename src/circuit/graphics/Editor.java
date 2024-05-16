package circuit.graphics;

import circuit.ProgramManager;
import circuit.components.Circuit;
import circuit.components.Pin;
import circuit.components.Wire;
import circuit.input.MouseInput;
import circuit.util.Util;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

// TODO: Add the pins to the circuit graphic class rather than displaying separately
public class Editor {
	// Pixel padding between the circuits in the circuit list
	public static final int CIRCUIT_WIDTH_PADDING = 8;
	// Pixel radius of the pins
	public static final int PIN_RADIUS = 5;
	// Pixel gap between the y coords of the input and output pins
	public static final int IOPIN_GAP = 18;
	// Pixel padding for how far into the circuit you have to click before deleting (to avoid accidental deletes)
	public static final int CIRCUIT_DELETE_PADDING = 3;

	// Represents the current circuit being built
	public Circuit currentCircuit;

	public List<PinGraphic> pins = new ArrayList<>();
	public List<WireGraphic> wires = new ArrayList<>();
	public List<CircuitGraphic> circuits = new ArrayList<>();
	// If drawing a line, stores the start pin location
	public PinGraphic startDrawLine = null;
	// If currently placing a circuit, stores the circuit graphic
	public CircuitGraphic currentlyHeldCircuit = null;

	// Represents the offset of the circuits in the circuit list when scrolling with the mouse wheel
	public int listXScrollOffset = 0;
	// Represents the graphics of all the circuits in the circuit list
	public List<CircuitGraphic> circuitList = new ArrayList<>();

	// Sets up the new circuit to be edited and creates the input and output pins
	public void initialiseNewCircuit(int i, int o) {
		currentCircuit = new Circuit("current", 0, 0, 0, 0);
		initIOPins(i, o);
	}

	// Generates the positions of the input and output pins based on the amount of pins requested
	public void initIOPins(int ins, int outs) {
		double startY;
		if (ins % 2 == 0)
			startY = (ProgramManager.HEIGHT - ProgramManager.CIRCUIT_LIST_HEIGHT) / 2.0 - (ins / 2.0 - 0.5) * IOPIN_GAP;
		else startY = (ProgramManager.HEIGHT - ProgramManager.CIRCUIT_LIST_HEIGHT) / 2.0 - ins / 2.0 * IOPIN_GAP;
		for (int i = 0; i < ins; i++) {
			Pin p = currentCircuit.addPin(Pin.INPUT);
			pins.add(new PinGraphic(p, (int) (PIN_RADIUS * 1.5), (int) (startY + i * IOPIN_GAP)));
		}

		if (outs % 2 == 0)
			startY = (ProgramManager.HEIGHT - ProgramManager.CIRCUIT_LIST_HEIGHT) / 2.0 - (outs / 2.0 - 0.5) * IOPIN_GAP;
		else startY = (ProgramManager.HEIGHT - ProgramManager.CIRCUIT_LIST_HEIGHT) / 2.0 - outs / 2.0 * IOPIN_GAP;
		for (int i = 0; i < outs; i++) {
			Pin p = currentCircuit.addPin(Pin.OUTPUT);
			pins.add(new PinGraphic(p, ProgramManager.WIDTH - (int) (PIN_RADIUS * 1.5), (int) (startY + i * IOPIN_GAP)));
		}
	}

	// Generates the positions of the pins attached to the currently held circuit when placing based on the amount of pins the circuit has
	public List<PinGraphic> generateSelectedCircuitPinGraphics(CircuitGraphic cg) {
		List<PinGraphic> ps = new ArrayList<>();

		int numIn = cg.getCircuit().getInputPins().size();
		int gap = IOPIN_GAP;// cg.getCircuit().getHeight() / (numIn + 1);
		double startY;
		if (numIn % 2 == 0) startY = cg.getY() + cg.getCircuit().getHeight() / 2 - (numIn / 2 - 0.5) * gap;
		else startY = cg.getY() + cg.getCircuit().getHeight() / 2 - (numIn / 2) * gap;
		for (int i = 0; i < numIn; i++) {
			PinGraphic pg = new PinGraphic(cg.getCircuit().getInputPins().get(i), cg.getX(), (int) (startY + i * gap));
			ps.add(pg);
		}

		int numOut = cg.getCircuit().getOutputPins().size();
		gap = IOPIN_GAP;// cg.getCircuit().getHeight() / (numOut + 1);
		if (numOut % 2 == 0) startY = cg.getY() + cg.getCircuit().getHeight() / 2 - (numOut / 2 - 0.5) * gap;
		else startY = cg.getY() + cg.getCircuit().getHeight() / 2 - (numOut / 2) * gap;
		for (int i = 0; i < numOut; i++) {
			PinGraphic pg = new PinGraphic(cg.getCircuit().getOutputPins().get(i), cg.getX() + cg.getCircuit().getWidth(), (int) (startY + i * gap));
			ps.add(pg);
		}

		return ps;
	}

	// Generates the coords (y is constant) for the circuit graphics in the circuit list
	public void generateCircuitListPositions() {
		int xo = CIRCUIT_WIDTH_PADDING;
		for (Circuit c : Circuit.CIRCUITS.values()) {
			// int width = renderer.g.getFontMetrics().stringWidth(c.getName()) * 2;
			int width = Math.max(c.getWidth(), 42);
			CircuitGraphic cg = new CircuitGraphic(c, xo + listXScrollOffset, ProgramManager.HEIGHT - ProgramManager.CIRCUIT_LIST_HEIGHT / 2 - 20, width, 40);
			circuitList.add(cg);
			xo += width + CIRCUIT_WIDTH_PADDING;
		}
	}

	// Handles any mouse events based on mouse button, whether a press or click, and what part of the screen is clicked
	public void event(int button, String type, double delta) {
		if (type.equals("CLICK")) {
			if (MouseInput.y <= ProgramManager.HEIGHT - ProgramManager.CIRCUIT_LIST_HEIGHT) {
				if (button == MouseEvent.BUTTON1) {
					if (currentlyHeldCircuit != null) placeCircuit(delta);
					else drawWireEvent();
				} else if (button == MouseEvent.BUTTON3) deleteEvent();
			} else {
				if (button == MouseEvent.BUTTON1) selectCircuitEvent();
				else if (button == MouseEvent.BUTTON3) cancelAllSelected();
			}
		}
		// else if (type.equals("PRESS")) {
		//     if (button == MouseEvent.BUTTON3) deleteEvent();
		// }
		// currentCircuit.tick(delta);
	}

	// Places the currently held circuit, adding the circuit and pins to the main circuit
	public void placeCircuit(double delta) {
		currentlyHeldCircuit.setX(MouseInput.x - currentlyHeldCircuit.getCircuit().getWidth() / 2);
		currentlyHeldCircuit.setY(MouseInput.y - currentlyHeldCircuit.getCircuit().getHeight() / 2);
		Circuit newCircuit = currentCircuit.addCircuit(currentlyHeldCircuit.getCircuit());
		CircuitGraphic cg = new CircuitGraphic(newCircuit, MouseInput.x - currentlyHeldCircuit.getCircuit().getWidth() / 2, MouseInput.y - currentlyHeldCircuit.getCircuit().getHeight() / 2, newCircuit.getWidth(), newCircuit.getHeight());
		List<PinGraphic> ps = generateSelectedCircuitPinGraphics(cg);
		pins.addAll(ps);
		circuits.add(cg);
	}

	// Handles drawing wires
	public void drawWireEvent() {
		boolean drawing = startDrawLine != null;

		for (PinGraphic pg : pins) {
			double dist = Util.distance(pg.getX(), pg.getY(), MouseInput.x, MouseInput.y);
			// If the pin is clicked on...
			if (dist <= PIN_RADIUS + 2) {
				if (startDrawLine == null) {
					// and we aren't drawing a line, start drawing a line from here
					startDrawLine = pg;
				} else if (startDrawLine == pg) {
					// and we clicked on the start pin, cancel the line
					startDrawLine = null;
				} else {
					// and we click on another pin, create a wire between these two pin
					Wire wire = currentCircuit.addWire(startDrawLine.getPin().getIds(), pg.getPin().getIds());
					WireGraphic wg = new WireGraphic(wire, startDrawLine.getX(), startDrawLine.getY(), pg.getX(), pg.getY());
					wires.add(wg);
					startDrawLine = null;
				}
				break;
			}
		}

		// If we aren't clicking on any pin, add the pin as an anchor point
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

	// Handles deleting components
	public void deleteEvent() {
		if (startDrawLine != null || currentlyHeldCircuit != null) {
			// If we are currently placing a wire/circuit, cancel it instead of deleting whats below
			cancelAllSelected();
		} else {
			// Checks to remove any pins and therefore any wires connected to the pin
			for (PinGraphic pg : pins) {
				if (Util.distance(pg.getX(), pg.getY(), MouseInput.x, MouseInput.y) < PIN_RADIUS + 2) {
					List<Wire> wiresToDelete = currentCircuit.attemptRemovePin(pg.getPin());
					if (pg.getPin().getType() == Pin.NONE) {
						pins.remove(pg);
						wires.removeIf(wg -> wiresToDelete.contains(wg.getWire()));
					}
					return;
				}
			}

			// Checks to remove any wires 
			for (WireGraphic wg : wires) {
				if (Util.distanceFromLine(wg.getX1(), wg.getY1(), wg.getX2(), wg.getY2(), MouseInput.x, MouseInput.y) < Renderer.WIRE_WIDTH) {
					currentCircuit.removeWire(wg.getWire());
					wires.remove(wg);
					return;
				}
			}

			// Checks to remove any circuit and therefore any wires connected to the pin
			for (CircuitGraphic cg : circuits) {
				if (Util.isPointWithinBox(MouseInput.x, MouseInput.y, cg.getX() + CIRCUIT_DELETE_PADDING, cg.getY() + CIRCUIT_DELETE_PADDING, cg.getWidth() - 2 * CIRCUIT_DELETE_PADDING, cg.getHeight() - 2 * CIRCUIT_DELETE_PADDING)) {
					List<Wire> wiresToDelete = currentCircuit.removeCircuit(cg.getCircuit());
					circuits.remove(cg);
					wires.removeIf(wg -> wiresToDelete.contains(wg.getWire()));
					pins.removeIf(pg -> pg.getPin().getIds().getCircuitID() == cg.getCircuit().getCircuitID());
					return;
				}
			}
		}
	}

	// Handles selecting a circuit from the circuit list
	public void selectCircuitEvent() {
		// If drawing a line, stop that rather than picking up a circuit
		if (startDrawLine != null) return;

		// If the mouse overlaps with the circuit box (taking into account the listXScrollOffset), pick it up
		for (CircuitGraphic cg : circuitList) {
			if (Util.isPointWithinBox(MouseInput.x, MouseInput.y, cg.getX() - CIRCUIT_WIDTH_PADDING + listXScrollOffset, cg.getY() - CIRCUIT_WIDTH_PADDING, cg.getWidth() + 2 * CIRCUIT_WIDTH_PADDING, cg.getHeight() + 2 * CIRCUIT_WIDTH_PADDING)) {
				currentlyHeldCircuit = cg.copy();
				return;
			}
		}
	}

	// Cancel any placement events 
	public void cancelAllSelected() {
		startDrawLine = null;
		currentlyHeldCircuit = null;
	}

	// Will update the input pins to the current circuit to be the next binary number for testing purposes
	public void increaseInputsByOne(double delta) {
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
		currentCircuit.tick(delta);
	}

	// Renders the graphics for the components placed, the component currently held and the circuit list
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

	// Creates a new circuit editor screen with the given number of input and output pins
	public void newEditor() {
		String res = JOptionPane.showInputDialog("Enter number of inputs and outputs (i o)");
		if (res == null) return;
		pins.clear();
		wires.clear();
		circuits.clear();
		circuitList.clear();
		listXScrollOffset = 0;
		currentlyHeldCircuit = null;
		startDrawLine = null;
		String[] tokens = res.split(" ");
		initialiseNewCircuit(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
	}
}