package circuit.components;

import circuit.util.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Circuit {
    public static final Map<String, Circuit> CIRCUITS = new LinkedHashMap<>();

    protected String name;
    protected int width, height;
    protected int circuitColor, textColor;
    protected int circuitID;
    protected int nextCircuitId;
    protected Circuit parent = null;

    protected List<Pin> inputPins = new ArrayList<>();
    protected List<Pin> outputPins = new ArrayList<>();
    protected List<Pin> pins = new ArrayList<>();
    protected List<Wire> wires = new ArrayList<>();
    protected List<Circuit> circuits = new ArrayList<>();

    public Circuit(String name, int width, int height, int circuitColor, int textColor) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.circuitColor = circuitColor;
        this.textColor = textColor;
        this.circuitID = 0;
        this.nextCircuitId = 1;
    }

    public Pin addPin(int type) {
        Pin p = new Pin(pins.size(), circuitID, type);
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

    public Circuit addCircuit(Circuit circuit) {
        circuit.parent = this;
        Circuit child = circuit.copy();
        circuits.add(child);
        addCircuit(child, this);
        return child;
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
        List<Pin> currentlyLit = new ArrayList<>();
        for (Pin p : pins) {
            if (p.getState()) currentlyLit.add(p);
        }
        HashMap<IDPair, Boolean> nextState = new HashMap<>();
        for (Wire wire : wires) {
            if (!nextState.containsKey(wire.getPid2())) nextState.put(wire.getPid2(), false);
            nextState.put(wire.getPid2(), nextState.get(wire.getPid2()) || getPinByID(wire.getPid1()).getState());
            // if (wire.getPid2().getCircuitID() == circuitID) {
            //     if (!nextState.containsKey(wire.getPid2())) nextState.put(wire.getPid2(), false);
            //     nextState.put(wire.getPid2(), getPinByID(wire.getPid1()).getState());
            // }
            // else {
            //     if (!nextState.containsKey(wire.getPid2())) nextState.put(wire.getPid2(), false);
            //     getCircuitByID(wire.getPid2().getCircuitID()).getPinByID(wire.getPid2()).setState(getPinByID(wire.getPid1()).getState());
            //     nextState.put(wire.getPid2(), getPinByID(wire.getPid2()).setState(getPinByID(wire.getPid1()).getState());
            // }
        }


        boolean changed = false;
        for (IDPair ids : nextState.keySet()) {
            Pin p = getPinByID(ids);
            if (p.getState() != nextState.get(ids)) changed = true;
            p.setState(nextState.get(ids));
        }

        for (Circuit circuit : circuits) {
            changed |= circuit.tick();
        }

        if (parent == null && changed) tick();
        return changed;
    }

    public List<Wire> interactWithPin(Pin pin) {
        List<Wire> wireIDsToDisconnect = new ArrayList<>();

        if (inputPins.contains(pin)) {
            pin.flip();
        } else if (!outputPins.contains(pin)) {
            for (Wire w : wires) {
                if (w.getPid1().equals(pin.getIds()) || w.getPid2().equals(pin.getIds())) {
                    wireIDsToDisconnect.add(w);
                }
            }
            for (Wire w : wireIDsToDisconnect) {
                wires.remove(w);
            }
            pins.remove(pin);
            inputPins.remove(pin);
            outputPins.remove(pin);
        }
        return wireIDsToDisconnect;
    }

    public void removeWire(Wire wire) {
        wires.remove(wire);
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
        Circuit ret = new Circuit(name, width, height, circuitColor, textColor);
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

    public void save() {
        try {
            Files.write(Paths.get("res/component_order.txt"), (name + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(name + "\n");
        sb.append(width + "\n");
        sb.append(height + "\n");
        sb.append(Integer.toHexString(circuitColor) + "\n");
        sb.append(Integer.toHexString(textColor) + "\n");

        HashMap<Circuit, Integer> map = new HashMap<>();
        HashMap<String, List<Integer>> toAdd = new HashMap<>();
        int id = 1;
        for (Circuit c : circuits) {
            if (!toAdd.containsKey(c.getName())) toAdd.put(c.getName(), new ArrayList<>());
            toAdd.get(c.getName()).add(id);
            map.put(c, id++);
        }
        for (String name : toAdd.keySet()) {
            sb.append("c " + name);
            for (int n : toAdd.get(name)) {
                sb.append(" " + n);
            }
            sb.append("\n");
        }

        sb.append("p I " + inputPins.size() + "\n");
        sb.append("p O " + outputPins.size() + "\n");
        int numNeutral = 0;
        for (Pin p : pins) {
            if (!inputPins.contains(p) && !outputPins.contains(p)) numNeutral++;
        }
        sb.append("p N " + numNeutral + "\n");

        for (Wire wire : wires) {
            sb.append("w " + wire.getPid1().getComponentID() + " ");
            Integer cid = map.get(getCircuitByID(wire.getPid1().getCircuitID()));
            if (cid == null) cid = 0;
            sb.append(cid + " ");
            sb.append(wire.getPid2().getComponentID() + " ");
            cid = map.get(getCircuitByID(wire.getPid2().getCircuitID()));
            if (cid == null) cid = 0;
            sb.append(cid + "\n");
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("res/components/" + name + ".txt"))) {
            bw.append(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Circuit circuit = new Circuit(data.get(0), Integer.parseInt(data.get(1)), Integer.parseInt(data.get(2)), Integer.parseInt(data.get(3), 16), Integer.parseInt(data.get(4), 16));
        HashMap<Integer, Integer> currCircuits = new HashMap<>();
        for (int i = 5; i < data.size(); i++) {
            String[] tokens = data.get(i).split(" ");
            if (tokens[0].equals("p")) {
                int amt = Integer.parseInt(tokens[2]);
                for (int n = 0; n < amt; n++) {
                    if (tokens[1].equals("I")) circuit.addPin(Pin.INPUT);
                    if (tokens[1].equals("O")) circuit.addPin(Pin.OUTPUT);
                    if (tokens[1].equals("N")) circuit.addPin(Pin.NONE);
                }
            } else if (tokens[0].equals("w")) {
                int c1 = tokens[2].equals("0") ? 0 : currCircuits.get(Integer.parseInt(tokens[2]));
                int c2 = tokens[4].equals("0") ? 0 : currCircuits.get(Integer.parseInt(tokens[4]));
                circuit.addWire(new IDPair(Integer.parseInt(tokens[1]), c1), new IDPair(Integer.parseInt(tokens[3]), c2));
            } else if (tokens[0].equals("c")) {
                Circuit toAdd = CIRCUITS.get(tokens[1]);
                for (int n = 2; n < tokens.length; n++) {
                    int newID = circuit.addCircuit(toAdd).circuitID;
                    currCircuits.put(Integer.parseInt(tokens[n]), newID);
                }
            }
        }
        CIRCUITS.put(circuit.name, circuit);
    }
}