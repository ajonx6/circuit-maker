package circuit.components;

public class ORGate extends Circuit {
    public ORGate() {
        super("or");
        addPin(new Pin(0, 0), Pin.INPUT);
        addPin(new Pin(1, 0), Pin.INPUT);
        addPin(new Pin(2, 0), Pin.OUTPUT);
    }
    
    public boolean tick() {
        boolean prev = pins.get(2).getState();
        pins.get(2).setState(pins.get(0).getState() || pins.get(1).getState());
        return prev ^ pins.get(2).getState();
    }

    public ORGate copy() {
        return new ORGate();
    }
}