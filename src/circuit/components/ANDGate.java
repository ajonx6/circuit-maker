package circuit.components;

// Represents an AND gate
public class ANDGate extends Circuit {
    public ANDGate() {
        super("and", 42, 36, 0x6C58A0, 0xFFFFFF);
        addPin(new Pin(0, 0), Pin.INPUT);
        addPin(new Pin(1, 0), Pin.INPUT);
        addPin(new Pin(2, 0), Pin.OUTPUT);
    }
    
    // Sets the output pin to the correct state based on the 2 inputs
    public void tick(double delta) {
        pins.get(2).setState(pins.get(0).getState() && pins.get(1).getState());
    }
    
    public ANDGate copy() {
        return new ANDGate();
    }
}