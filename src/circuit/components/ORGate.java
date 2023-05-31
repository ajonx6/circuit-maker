package circuit.components;

public class ORGate extends Circuit {
    public ORGate() {
        super("or", 42, 36, 0x698958, 0xFFFFFF);
        addPin(new Pin(0, 0), Pin.INPUT);
        addPin(new Pin(1, 0), Pin.INPUT);
        addPin(new Pin(2, 0), Pin.OUTPUT);
    }
    
    public void tick(double delta) {
        pins.get(2).setState(pins.get(0).getState() || pins.get(1).getState());
        //
        // if (pins.get(2).shouldBeOn()) pins.get(2).incrementDelayTime(delta);
        // else pins.get(2).setDelayTime(0);
        //
        // pins.get(2).setShouldBeOn(pins.get(0).getState() || pins.get(1).getState());
        // if (pins.get(2).shouldTurnOn()) {
        //     pins.get(2).setState(true);
        //     pins.get(2).setDelayTime(pins.get(2).getPinDelay());
        // } else {
        //     pins.get(2).setState(false);
        // }
    }

    public ORGate copy() {
        return new ORGate();
    }
}