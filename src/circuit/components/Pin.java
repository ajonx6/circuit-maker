package circuit.components;

import org.w3c.dom.ls.LSOutput;

public class Pin {
    public static final int NONE = 0;
    public static final int INPUT = 1;
    public static final int OUTPUT = 2;

    private IDPair ids;
    private int type = NONE;
    private boolean state = false;

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

    public void setState(boolean state) {
        this.state = state;
    }

    public void flip() {
        this.state = !this.state;
    }

    public boolean getState() {
        return state;
    }

    public int getType() {
        return type;
    }

    public Pin copy() {
        Pin ret = new Pin(ids.getComponentID(), ids.getCircuitID());
        return ret;
    }
    
    public String toString() {
        return state ? "1" : "0";
    }
}