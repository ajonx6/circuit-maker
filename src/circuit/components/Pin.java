package circuit.components;

public class Pin {
    public static final int NONE = 0;
    public static final int INPUT = 1;
    public static final int OUTPUT = 2;
    
    private IDPair ids;
    private boolean state = false;
    
    public Pin(int id, int cid) {
        ids = new IDPair(id, cid);
    }

    public IDPair getIds() {
        return ids;
    }
    
    public void setState(boolean state) {
        this.state = state;
    }
    
    public boolean getState() {
        return state;
    }
    
    public Pin copy() {
        Pin ret = new Pin(ids.getComponentID(), ids.getCircuitID());
        return ret;
    }
}