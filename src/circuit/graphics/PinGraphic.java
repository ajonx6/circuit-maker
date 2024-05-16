package circuit.graphics;

import circuit.components.Pin;

// Represents a pin displayed on the screen
public class PinGraphic {
    private Pin pin;
    private int x, y;

    public PinGraphic(Pin pin, int x, int y) {
        this.pin = pin;
        this.x = x;
        this.y = y;
    }

    public Pin getPin() {
        return pin;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}