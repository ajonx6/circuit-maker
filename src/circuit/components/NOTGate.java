package circuit.components;

public class NOTGate extends Circuit {
    public NOTGate() {
        super("not", 42, 36, 0xAD6685, 0xFFFFFF);
        addPin(new Pin(0, 0), Pin.INPUT);
        addPin(new Pin(1, 0), Pin.OUTPUT);
    }
    
    public boolean tick() {
        boolean prev = pins.get(1).getState();
        pins.get(1).setState(!pins.get(0).getState());
        return prev ^ pins.get(1).getState();
    }

    public NOTGate copy() {
        return new NOTGate();
    }
}