package circuit.components;

import circuit.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Circuit {
    public static final HashMap<String, Circuit> CIRCUITS = new HashMap<>();

    protected String name;
    protected int circuitID;
    protected int nextCircuitId;
    protected Circuit parent = null;

    protected List<Pin> inputPins = new ArrayList<>();
    protected List<Pin> outputPins = new ArrayList<>();
    protected List<Pin> pins = new ArrayList<>();
    protected List<Wire> wires = new ArrayList<>();
    protected List<Circuit> circuits = new ArrayList<>();

    public Circuit(String name) {
        this.name = name;
        this.circuitID = 0;
        this.nextCircuitId = 1;
    }

    public Pin addPin(int type) {
        Pin p = new Pin(pins.size(), circuitID);
        return addPin(p, type);
    }

    public Pin addPin(Pin pin, int type) {
        pins.add(pin);
        if (type == Pin.INPUT) inputPins.add(pin);
        else if (type == Pin.OUTPUT) outputPins.add(pin);
        return pin;
    }

    public Wire addWire(IDPair pid1, IDPair pid2) {
        Wire wire = new Wire(wires.size(), circuitID);
        wire.connectP1(pid1);
        wire.connectP2(pid2);
        return addWire(wire);
    }

    public Wire addWire(Wire wire) {
        wires.add(wire);
        return wire;
    }

    public int addCircuit(Circuit circuit) {
        circuit.parent = this;
        Circuit child = circuit.copy();
        circuits.add(child);
        addCircuit(child, this);
        return child.circuitID;
    }

    private void addCircuit(Circuit circuit, Circuit top) {
        circuit.circuitID = top.nextCircuitId;
        for (Pin p : circuit.pins) {
            p.getIds().setCircuitID(nextCircuitId);
        }
        for (Wire w : circuit.wires) {
            int diff = nextCircuitId - w.getIds().getCircuitID();
            w.getIds().setCircuitID(nextCircuitId);
            w.getPid1().setCircuitID(w.getPid1().getCircuitID() + diff);
            w.getPid2().setCircuitID(w.getPid2().getCircuitID() + diff);
        }
        top.nextCircuitId++;
        for (Circuit child : circuit.circuits) {
            addCircuit(child, top);
        }
    }

    public boolean tick() {
        boolean changed = false;
        for (Wire wire : wires) {
            boolean previous = false;
            if (wire.getPid2().getCircuitID() == circuitID) {
                previous = getPinByID(wire.getPid2()).getState();
                getPinByID(wire.getPid2()).setState(getPinByID(wire.getPid1()).getState());
                changed |= (previous ^ getPinByID(wire.getPid2()).getState());
            }
            else {
                previous = getCircuitByID(wire.getPid2().getCircuitID()).getPinByID(wire.getPid2()).getState();
                getCircuitByID(wire.getPid2().getCircuitID()).getPinByID(wire.getPid2()).setState(getPinByID(wire.getPid1()).getState());
                changed |= (previous ^ getCircuitByID(wire.getPid2().getCircuitID()).getPinByID(wire.getPid2()).getState());
            }
        }
        for (Circuit circuit : circuits) {
            changed |= circuit.tick();
        }
        if (parent == null && changed) tick();
        return changed;
    }

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

    public Circuit getCircuitByID(int id) {
        for (Circuit c : circuits) {
            if (c.circuitID == id) return c;
        }
        return null;
    }

    public void print() {
        // System.out.println("Circuit: " + name + " id=" + circuitID + (parent != null ? (", parent id=" + parent.circuitID + ")") : ""));
        // System.out.println("Pins (no.: " + pins.size() + ")");
        int i = 0;
        for (Pin p : pins) {
            System.out.println("\t" + p.getIds() + ": " + (p.getState() ? "1" : "0"));
            if (i == 2) System.out.println();
            i++;
        }
        // System.out.println("Wires (no.: " + wires.size() + ")");
        // for (Wire w : wires) {
        //     System.out.println("\t" + w.getIds() + ": " + w.getPid1() + ", " + w.getPid2());
        // }
        //
        // System.out.println("=============================");
        // for (Circuit c : circuits) {
        //     c.print();
        // }
    }

    public Circuit copy() {
        Circuit ret = new Circuit(name);
        for (Pin p : pins) {
            ret.addPin(p.copy(), inputPins.contains(p) ? Pin.INPUT : (outputPins.contains(p) ? Pin.OUTPUT : Pin.NONE));
        }
        for (Wire w : wires) {
            ret.addWire(w.copy());
        }
        for (Circuit c : circuits) {
            ret.addCircuit(c.copy());
        }
        return ret;
    }

    public Circuit getParent() {
        return parent;
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

    public static void loadAllCircuits() {
        CIRCUITS.put("and", new ANDGate());
        CIRCUITS.put("or", new ORGate());
        CIRCUITS.put("not", new NOTGate());

        List<String> order = Util.load("component_order.txt");
        for (String name : order) {
            loadCircuit(name + ".txt");
        }
    }

    public static void loadCircuit(String name) {
        List<String> data = Util.load("components/" + name);
        Circuit circuit = new Circuit(data.get(0));
        HashMap<Integer, Integer> currCircuits = new HashMap<>();
        for (int i = 1; i < data.size(); i++) {
            String[] tokens = data.get(i).split(" ");
            if (tokens[0].equals("p")) {
                if (tokens[1].equals("I")) circuit.addPin(Pin.INPUT);
                if (tokens[1].equals("O")) circuit.addPin(Pin.OUTPUT);
                if (tokens[1].equals("N")) circuit.addPin(Pin.NONE);
            } else if (tokens[0].equals("w")) {
                int c1 = tokens[2].equals("0") ? 0 : currCircuits.get(Integer.parseInt(tokens[2]));
                int c2 = tokens[4].equals("0") ? 0 : currCircuits.get(Integer.parseInt(tokens[4]));
                circuit.addWire(new IDPair(Integer.parseInt(tokens[1]), c1), new IDPair(Integer.parseInt(tokens[3]), c2));
            } else if (tokens[0].equals("c")) {
                Circuit toAdd = CIRCUITS.get(tokens[1]);
                int newID = circuit.addCircuit(toAdd);
                currCircuits.put(Integer.parseInt(tokens[2]), newID);
            }
        }
        CIRCUITS.put(circuit.name, circuit);
    }
}