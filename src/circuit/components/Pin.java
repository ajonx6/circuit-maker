package circuit.components;

// Represents a pin that connects wires within the circuit
public class Pin {
    public static final int NONE = 0;
    public static final int INPUT = 1;
    public static final int OUTPUT = 2;

    private IDPair ids;
    // Whether an input pin, output pin or simply an anchor pin
    private int type = NONE;
    // Whether the pin is on or off
    private boolean state = false;
    // Whether the pin should be trying to turn on or not
    private boolean shouldBeOn = false;
    // Whether the pin represents the clock input to a circuit
    private boolean isClockPin = false;
    
    // Represents the time taken between receiving a 1 and outputting a 1 (gate delay)
    private double pinDelay;
    private double delayTime = 0;

    public Pin() {
    }

    public Pin(int type) {
        this.type = type;
    }

    public Pin(int id, int cid) {
        this.ids = new IDPair(id, cid);
    }

    public Pin(int id, int cid, int type) {
        this.ids = new IDPair(id, cid);
        this.type = type;
    }

    public IDPair getIds() {
        return ids;
    }

    public void setIds(int id, int cid) {
        ids = new IDPair(id, cid);
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public void flip() {
        this.state = !this.state;
    }

    public boolean shouldBeOn() {
        return shouldBeOn;
    }

    public void setShouldBeOn(boolean shouldBeOn) {
        this.shouldBeOn = shouldBeOn;
    }
    
    public int getType() {
        return type;
    }

    public double getPinDelay() {
        return pinDelay;
    }

    public void setPinDelay(double pinDelay) {
        this.pinDelay = pinDelay;
    }

    public void incrementDelayTime(double delta) {
        this.delayTime += delta;
    }
    
    public void setDelayTime(double d) {
        this.delayTime = d;
    }

    public double getDelayTime() {
        return delayTime;
    }

    // Returns if the pin should be on for longer than the pin delay
    public boolean shouldTurnOn() {
        return shouldBeOn && this.delayTime >= this.pinDelay;
    }

    public void setClockPin() {
        this.isClockPin = true;
    }

    public boolean isClockPin() {
        return isClockPin;
    }

    public Pin copy() {
        Pin ret = new Pin(ids.getComponentID(), ids.getCircuitID());
        if (isClockPin()) ret.setClockPin();
        return ret;
    }
    
    public String toString() {
        return state ? "1" : "0";
    }
}