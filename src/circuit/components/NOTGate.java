package circuit.components;

// Represents a NOT gate
public class NOTGate extends Circuit {
    public NOTGate() {
        super("not", 42, 36, 0xAD6685, 0xFFFFFF);
        addPin(new Pin(0, 0), Pin.INPUT);
        addPin(new Pin(1, 0), Pin.OUTPUT);
    }

    // Sets the output pin to the correct state based on the input
    public void tick(double delta) {
        pins.get(1).setState(!pins.get(0).getState());
    }

    public NOTGate copy() {
        return new NOTGate();
    }
}