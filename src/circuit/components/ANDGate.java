package circuit.components;

public class ANDGate extends Circuit {
    public ANDGate() {
        super("and", 42, 36, 0x6C58A0, 0xFFFFFF);
        addPin(new Pin(0, 0), Pin.INPUT);
        addPin(new Pin(1, 0), Pin.INPUT);
        addPin(new Pin(2, 0), Pin.OUTPUT);
    }
    
    public void tick(double delta) {
        pins.get(2).setState(pins.get(0).getState() && pins.get(1).getState());

        // if (pins.get(2).shouldBeOn()) pins.get(2).incrementDelayTime(delta);
        // else pins.get(2).setDelayTime(0);
        //
        // pins.get(2).setShouldBeOn(pins.get(0).getState() && pins.get(1).getState());
        // if (pins.get(2).shouldTurnOn()) {
        //     pins.get(2).setState(true);
        //     pins.get(2).setDelayTime(pins.get(2).getPinDelay());
        // } else {
        //     pins.get(2).setState(false);
        // }
    }
    
    public ANDGate copy() {
        return new ANDGate();
    }
}