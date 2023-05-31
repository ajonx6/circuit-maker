package circuit.components;

public class NOTGate extends Circuit {
    public NOTGate() {
        super("not", 42, 36, 0xAD6685, 0xFFFFFF);
        addPin(new Pin(0, 0), Pin.INPUT);
        addPin(new Pin(1, 0), Pin.OUTPUT);
    }
    
    public void tick(double delta) {
        // if (p.shouldBeOn()) p.incrementDelayTime(delta);
        // else p.resetDelayTime(0);

        // if (circuitID > 0 && !inputPins.contains(p) && !outputPins.contains(p)) {
        //     p.setShouldBeOn(nextState.get(ids));
        //     // p.setState(p.shouldBeOn());
        //     // if (p.shouldBeOn() && p.delays[outputPins.indexOf(p)] >= gateDelay) {
        //     if (p.shouldTurnOn()) {
        //         p.setState(true);
        //         // p.setdelays[outputPins.indexOf(p)] = gateDelay;
        //         // System.out.println("HEYEYEY1");
        //         p.resetDelayTime(p.getPinDelay());
        //     } else {
        //         // System.out.println("HEYEYEY2");
        //         p.setState(false);
        //     }
        // } else p.setState(nextState.get(ids));
        
        // if (pins.get(1).shouldBeOn()) pins.get(1).incrementDelayTime(delta);
        // else pins.get(1).setDelayTime(0);
        //
        // pins.get(1).setShouldBeOn(!pins.get(0).getState());
        // if (pins.get(1).shouldTurnOn()) {
        //     pins.get(1).setState(true);
        //     pins.get(1).setDelayTime(pins.get(1).getPinDelay());
        // } else {
        //     pins.get(1).setState(false);
        // }
        //
        pins.get(1).setState(!pins.get(0).getState());
        // return prev ^ pins.get(1).getState();
    }

    public NOTGate copy() {
        return new NOTGate();
    }
}