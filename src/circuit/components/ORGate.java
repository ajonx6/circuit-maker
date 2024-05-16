package circuit.components;

// Represents an OR gate
public class ORGate extends Circuit {
    public ORGate() {
        super("or", 42, 36, 0x698958, 0xFFFFFF);
        addPin(new Pin(0, 0), Pin.INPUT);
        addPin(new Pin(1, 0), Pin.INPUT);
        addPin(new Pin(2, 0), Pin.OUTPUT);
    }

    // Sets the output pin to the correct state based on the 2 inputs
    public void tick(double delta) {
        pins.get(2).setState(pins.get(0).getState() || pins.get(1).getState());
    }

    public ORGate copy() {
        return new ORGate();
    }
}