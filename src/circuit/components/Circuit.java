package circuit.components;

import circuit.util.Util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

// Represents a circuit that can contain pins, wires and other circuits
public class Circuit {
	// The maximum pin delay allowed
	public static final double MAX_PIN_DELAY = 0.025;
	// Maps the name of the circuit to the circuit object
	public static final Map<String, Circuit> CIRCUITS = new LinkedHashMap<>();

	// The name of the circuit
	protected String name;
	// The width and height of the circuit displayed in the renderer
	protected int width, height;
	// Colour of the circuit box and text in the renderer
	protected int circuitColor, textColor;
	// The current id of the circuit (matches the IDPair.circuitID of components in this circuit)
	protected int circuitID;
	// The next id to give the child circuit when added
	protected int nextCircuitId;
	// The parent of this circuit (current editing circuit is null)
	protected Circuit parent = null;

	protected List<Pin> inputPins = new ArrayList<>();
	protected List<Pin> outputPins = new ArrayList<>();
	protected List<Pin> pins = new ArrayList<>();
	protected List<Wire> wires = new ArrayList<>();
	protected List<Circuit> circuits = new ArrayList<>();

	// The pin used to clock/update the circuit
	protected Pin clockPin;
	// Whether the clock has forced a reset at the start of the placement
	protected boolean performedClockReset = false;
	// The time given to reset the circuit
	protected double timeForClockReset = 0.1;
	protected double clockResetTimer = 0;

	// The time given for an update to happen
	protected double timePerUpdate = 0.002;
	protected double updateTimer = 0;

	public Circuit(String name, int width, int height, int circuitColor, int textColor) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.circuitColor = circuitColor;
		this.textColor = textColor;
		this.circuitID = 0;
		this.nextCircuitId = 1;
	}

	// Creates a pin with the correct componentID of a specific type to the current circuit
	public Pin addPin(int type) {
		Pin p = new Pin(pins.size(), circuitID, type);
		return addPin(p, type);
	}

	// Adds a pin of a specific type to the current circuit
	public Pin addPin(Pin pin, int type) {
		pins.add(pin);
		pin.setType(type);
		if (type == Pin.INPUT) inputPins.add(pin);
		else if (type == Pin.OUTPUT) outputPins.add(pin);
		return pin;
	}

	// Returns the pin with the given matching id (recursively searches, both parts of the id have to match)
	public Pin getPinByID(IDPair ids) {
		for (Pin p : pins) {
			if (p.getIds().equals(ids)) return p;
		}
		for (Circuit circuit : circuits) {
			Pin result = circuit.getPinByID(ids);
			if (result != null) return result;
		}
		return null;
	}

	// Attempts to remove a pin and if successful, returns the wires that will need to be deleted too
	public List<Wire> attemptRemovePin(Pin pin) {
		if (pin.getType() == Pin.INPUT) {
			// If the pin is an input for a circuit, don't remove it but simply toggle it
			pin.flip();
			return new ArrayList<>();
		} else if (pin.getType() == Pin.OUTPUT) {
			// If the pin is an output for a circuit, no need to do anything
			return new ArrayList<>();
		}

		// for (Circuit c : circuits) {
		// 	if (c.inputPins.contains(pin) || c.outputPins.contains(pin)) return new ArrayList<>();
		// }

		List<Wire> wiresToDelete = new ArrayList<>();
		// Adds the wire to be removed if either end is the deleted pin
		for (Wire w : wires) {
			if (w.getPid1().equals(pin.getIds()) || w.getPid2().equals(pin.getIds())) {
				wiresToDelete.add(w);
			}
		}

		// Removes the pin 
		pins.remove(pin);
		
		// Removes the wires from this circuit
		for (Wire w : wiresToDelete) {
			wires.remove(w);
		}

		return wiresToDelete;
	}

	// Creates a wire with the correct componentID and pin ids to the current circuit
	public Wire addWire(IDPair pid1, IDPair pid2) {
		Wire wire = new Wire(wires.size(), circuitID);
		wire.connectP1(pid1);
		wire.connectP2(pid2);
		return addWire(wire);
	}

	// Adds a wire to the current circuit
	public Wire addWire(Wire wire) {
		wires.add(wire);
		return wire;
	}

	// Returns the wire with the given matching id (recursively searches, both parts of the id have to match)
	public Wire getWireByID(IDPair ids) {
		for (Wire w : wires) {
			if (w.getIds().equals(ids)) return w;
		}
		for (Circuit circuit : circuits) {
			Wire result = circuit.getWireByID(ids);
			if (result != null) return result;
		}
		return null;
	}

	// Removes a wire from the circuit
	public void removeWire(Wire wire) {
		wires.remove(wire);
	}

	// Adds a child circuit to the current circuit
	public Circuit addCircuit(Circuit circuit) {
		// Clones the circuit to add
		Circuit child = circuit.copy();
		// Sets the childs parent to the current circuit
		child.parent = this;
		// Adds the child circuit to this circuit
		circuits.add(child);
		updateCircuitIDs(child, this);
		return child;
	}

	// Recursively update the circuit ids of the child circuits
	private void updateCircuitIDs(Circuit circuit, Circuit parent) {
		// Generate the delays for the pins of the child circuit
		circuit.generateGateDelay();
		// Sets the circuit id for the child circuit based on the next available id
		circuit.circuitID = parent.nextCircuitId++;
		
		// Updates the circuit ids for the pins of the child circuit
		for (Pin p : circuit.pins) {
			p.getIds().setCircuitID(nextCircuitId);
		}
		
		// Updates the circuit ids for the wires and pin reference ids
		for (Wire w : circuit.wires) {
			int diff = nextCircuitId - w.getIds().getCircuitID();
			w.getIds().setCircuitID(nextCircuitId);
			w.getPid1().setCircuitID(w.getPid1().getCircuitID() + diff);
			w.getPid2().setCircuitID(w.getPid2().getCircuitID() + diff);
		}
		
		// Update the ids for the children of this child circuit
		for (Circuit child : circuit.circuits) {
			updateCircuitIDs(child, parent);
		}

		// Resets all the input pins to false
		for (Pin p : circuit.inputPins) {
			p.setState(false);
			p.setShouldBeOn(false);
		}

		// If there is a clock pin, set it to 1 and start updating the circuit
		// It will soon be set back to 0
		if (parent.name.equals("current") && circuit.clockPin != null) {
			circuit.clockPin.setState(true);
			circuit.clockPin.setShouldBeOn(true);
			tick(0.001);
		}
	}

	// Returns the circuit with the given circuit id (recursively searches)
	public Circuit getCircuitByID(int id) {
		if (id == circuitID) return this;
		for (Circuit c : circuits) {
			if (c.circuitID == id) return c;
		}
		return null;
	}

	// Removes a child circuit and return the wires that will need to be deleted too
	public List<Wire> removeCircuit(Circuit circuit) {
		List<Wire> wiresToDelete = new ArrayList<>();

		// If the wires start or end point has the same circuit id as the circuit being deleted, remove the wire
		for (Wire wire : wires) {
			if (wire.getPid1().getCircuitID() == circuit.circuitID || wire.getPid2().getCircuitID() == circuit.circuitID) {
				wiresToDelete.add(wire);
			}
		}
		for (Wire wire : wiresToDelete) {
			wires.remove(wire);
		}
		circuits.remove(circuit);

		return wiresToDelete;
	}

	// For all the anchor pins in the circuit set the delay to a random value between 0 and MAX_PIN_DELAY
	public void generateGateDelay() {
		for (Pin p : pins) {
			if (inputPins.contains(p) || outputPins.contains(p)) continue;
			p.setPinDelay(Math.random() * MAX_PIN_DELAY);
		}
	}

	// Update the circuit - update the states of wires based on their start pin and update the pins based on incoming wires  
	public void tick(double delta) {
		// Keeps the updating a constant specified rate
		updateTimer += delta;
		if (updateTimer < timePerUpdate) return;
		updateTimer -= timePerUpdate;

		// If there is a clock pin reset occurring and a certain time has elapsed, reset the clock pin (hopefully the circuit is reset) 
		if (!performedClockReset && clockPin != null) {
			clockResetTimer += delta;
			if (clockResetTimer >= timeForClockReset) {
				performedClockReset = true;
				clockPin.setState(false);
				clockPin.setShouldBeOn(false);
			}
		}

		// Represents the states of the pins after the update is done
		HashMap<IDPair, Boolean> nextState = new HashMap<>();
		for (Wire wire : wires) {
			// If this is the first time we come across this pin as an output of a wire, initially set its next state to 0
			if (!nextState.containsKey(wire.getPid2())) nextState.put(wire.getPid2(), false);
			// If we have already come across a wire that set this node to 1, keep it 1
			// Or set it to 1 if the input pin for the wire is 1 
			nextState.put(wire.getPid2(), nextState.get(wire.getPid2()) || getPinByID(wire.getPid1()).getState());
		}
		
		// If the pin is not an input, and it doesn't have a mapping above, this mean its a pin with no incoming wires, so set it to 0
		for (Pin p : pins) {
			if (!inputPins.contains(p) && !nextState.containsKey(p.getIds())) nextState.put(p.getIds(), false);
		}
		
		if (circuitID > 0) {
			for (Pin p : pins) {
				if (inputPins.contains(p) || outputPins.contains(p)) continue;
				if (p.shouldBeOn()) p.incrementDelayTime(timePerUpdate);
				else p.setDelayTime(0);
			}
		}

		// Loops over the updated states 
		for (IDPair ids : nextState.keySet()) {
			Pin p = getPinByID(ids);
			// If the pin is an anchor pin
			if (circuitID > 0 && !inputPins.contains(p) && !outputPins.contains(p)) {
				// Means the pin should turn on/off after the pin delay time
				p.setShouldBeOn(nextState.get(ids));
				
				if (p.shouldTurnOn()) {
					// If should turn on now, since it should be on for more than its delay, turn it on
					p.setState(true);
					p.setDelayTime(p.getPinDelay());
				} else {
					// Else turn/keep the pin off 
					p.setState(false);
				}
			} else p.setState(nextState.get(ids));
		}

		// Recursively update the pins of the children of this circuit
		for (Circuit circuit : circuits) {
			circuit.tick(delta);
		}
	}

	// Returns a String representation of the circuit data, pin states and wire start/end ids
	public String toString() {
		StringBuilder ret = new StringBuilder("Circuit: " + name + " id=" + circuitID + (parent != null ? (", parent id=" + parent.circuitID + ")") : ""));
		ret.append("\nPins (").append(pins.size()).append(")");
		int i = 0;
		for (Pin p : pins) {
			ret.append("\n\t").append(p.getIds()).append(": ").append(p.getState() ? "1" : "0");
			if (i == 2) ret.append("\n");
			i++;
		}
		ret.append("\nWires (").append(wires.size()).append(")");
		for (Wire w : wires) {
			ret.append("\n\t").append(w.getIds()).append(": ").append(w.getPid1()).append(", ").append(w.getPid2());
		}
		ret.append("\n=============================");

		return ret.toString();
	}

	// Prints a String representation of the circuit data, pin states and wire start/end ids
	public void print() {
		System.out.println("Circuit: " + name + " id=" + circuitID + (parent != null ? (", parent id=" + parent.circuitID + ")") : ""));
		System.out.println("Pins (no.: " + pins.size() + ")");
		int i = 0;
		for (Pin p : pins) {
			System.out.println("\t" + p.getIds() + ": " + (p.getState() ? "1" : "0"));
			if (i == 2) System.out.println();
			i++;
		}
		System.out.println("Wires (no.: " + wires.size() + ")");
		for (Wire w : wires) {
			System.out.println("\t" + w.getIds() + ": " + w.getPid1() + ", " + w.getPid2());
		}

		System.out.println("=============================");
		for (Circuit c : circuits) {
			c.print();
		}
	}

	// Performs a deep copy of the circuit
	public Circuit copy() {
		Circuit ret = new Circuit(name, width, height, circuitColor, textColor);
		for (Pin p : pins) {
			Pin copy = p.copy();
			ret.addPin(copy, inputPins.contains(p) ? Pin.INPUT : (outputPins.contains(p) ? Pin.OUTPUT : Pin.NONE));
			if (copy.isClockPin()) ret.clockPin = copy;
		}
		for (Wire w : wires) {
			ret.addWire(w.copy());
		}
		for (Circuit c : circuits) {
			ret.addCircuit(c.copy());
		}
		return ret;
	}

	// Resets the lists of components in this circuit
	public void reset() {
		inputPins.clear();
		outputPins.clear();
		pins.clear();
		wires.clear();
		circuits.clear();
	}

	public Circuit getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getCircuitColor() {
		return circuitColor;
	}

	public void setCircuitColor(int circuitColor) {
		this.circuitColor = circuitColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public int getCircuitID() {
		return circuitID;
	}

	public List<Pin> getInputPins() {
		return inputPins;
	}

	public List<Pin> getOutputPins() {
		return outputPins;
	}

	public List<Pin> getPins() {
		return pins;
	}

	public List<Wire> getWires() {
		return wires;
	}

	public List<Circuit> getCircuits() {
		return circuits;
	}

	// Saves the current circuit to a file
	public void save(int clockId) {
		try {
			Files.write(Paths.get("res/component_order.txt"), (name + "\n").getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// First 5 lines represent this data
		StringBuilder sb = new StringBuilder();
		sb.append(name).append("\n");
		sb.append(width).append("\n");
		sb.append(height).append("\n");
		sb.append(Integer.toHexString(circuitColor)).append("\n");
		sb.append(Integer.toHexString(textColor)).append("\n");
		
		// Represents circuits as "c circuit_name cid1 ... cidn"
		HashMap<Circuit, Integer> map = new HashMap<>();
		HashMap<String, List<Integer>> toAdd = new HashMap<>();
		int id = 1;
		for (Circuit c : circuits) {
			if (!toAdd.containsKey(c.getName())) toAdd.put(c.getName(), new ArrayList<>());
			toAdd.get(c.getName()).add(id);
			map.put(c, id++);
		}
		for (String name : toAdd.keySet()) {
			sb.append("c ").append(name);
			for (int n : toAdd.get(name)) {
				sb.append(" ").append(n);
			}
			sb.append("\n");
		}

		// Has three lines of the form "p type n"
		// Type is either I (in), O (out) or N (none) and n is the count of each type
		sb.append("p I ").append(inputPins.size()).append("\n");
		sb.append("p O ").append(outputPins.size()).append("\n");
		int numNeutral = 0;
		for (Pin p : pins) {
			if (!inputPins.contains(p) && !outputPins.contains(p)) numNeutral++;
		}
		sb.append("p N ").append(numNeutral).append("\n");

		// Optionally, if there is a clock pin, write "l pid"
		if (clockId >= 0) sb.append("l ").append(clockId).append("\n");

		// Represents wires as "w pid1 cid1 pid2 cid2"
		for (Wire wire : wires) {
			sb.append("w ").append(wire.getPid1().getComponentID()).append(" ");
			Integer cid = map.get(getCircuitByID(wire.getPid1().getCircuitID()));
			if (cid == null) cid = 0;
			sb.append(cid).append(" ");
			sb.append(wire.getPid2().getComponentID()).append(" ");
			cid = map.get(getCircuitByID(wire.getPid2().getCircuitID()));
			if (cid == null) cid = 0;
			sb.append(cid).append("\n");
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter("res/components/" + name + ".txt"))) {
			bw.append(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Loads all the circuits to the map
	// Adds and, or and not by default since they are special cases
	// Then loads the components based on the order specified in the order file
	public static void loadAllCircuits() {
		CIRCUITS.put("and", new ANDGate());
		CIRCUITS.put("or", new ORGate());
		CIRCUITS.put("not", new NOTGate());

		List<String> order = Util.load("component_order.txt");
		for (String name : order) {
			Circuit circuit = loadCircuit(name + ".txt");
			CIRCUITS.put(name, circuit);
		}
	}

	// Parses the data as specified in the "save" function above
	public static Circuit loadCircuit(String name) {
		List<String> data = Util.load("components/" + name);
		Circuit circuit = new Circuit(data.get(0), Integer.parseInt(data.get(1)), Integer.parseInt(data.get(2)), Integer.parseInt(data.get(3), 16), Integer.parseInt(data.get(4), 16));
		HashMap<Integer, Integer> currCircuits = new HashMap<>();

		int clockID = -1;
		for (int i = 5; i < data.size(); i++) {
			String[] tokens = data.get(i).split(" ");
			switch (tokens[0]) {
				case "p" -> {
					int amt = Integer.parseInt(tokens[2]);
					for (int n = 0; n < amt; n++) {
						if (tokens[1].equals("I")) circuit.addPin(Pin.INPUT);
						if (tokens[1].equals("O")) circuit.addPin(Pin.OUTPUT);
						if (tokens[1].equals("N")) circuit.addPin(Pin.NONE);
					}
				}
				case "l" -> {
					clockID = Integer.parseInt(tokens[1]);
				}
				case "w" -> {
					int c1 = tokens[2].equals("0") ? 0 : currCircuits.get(Integer.parseInt(tokens[2]));
					int c2 = tokens[4].equals("0") ? 0 : currCircuits.get(Integer.parseInt(tokens[4]));
					circuit.addWire(new IDPair(Integer.parseInt(tokens[1]), c1), new IDPair(Integer.parseInt(tokens[3]), c2));
				}
				case "c" -> {
					Circuit toAdd = CIRCUITS.get(tokens[1]);
					for (int n = 2; n < tokens.length; n++) {
						int newID = circuit.addCircuit(toAdd).circuitID;
						currCircuits.put(Integer.parseInt(tokens[n]), newID);
					}
				}
			}
		}
		
		// If a clock pin is a detected, set it up
		if (clockID != -1) {
			circuit.pins.get(clockID).setClockPin();
			circuit.clockPin = circuit.pins.get(clockID);
		}

		return circuit;
	}
}