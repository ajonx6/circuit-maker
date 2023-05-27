package circuit.graphics;

import circuit.components.Circuit;

public class CircuitGraphic {
    private Circuit circuit;
    private int x, y;
    private int width, height;

    public CircuitGraphic(Circuit circuit, int x, int y, int width, int height) {
        this.circuit = circuit;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Circuit getCircuit() {
        return circuit;
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
    public CircuitGraphic copy() {
        return new CircuitGraphic(circuit.copy(), x, y, width, height);
    }
}