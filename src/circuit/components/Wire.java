package circuit.components;

public class Wire {
    private IDPair ids;
    private IDPair pid1, pid2;

    public Wire() {}
    
    public Wire(int id, int cid) {
        ids = new IDPair(id, cid);
    }

    public void connectP1(IDPair p1) {
        pid1 = p1;
    }

    public void connectP2(IDPair p2) {
        pid2 = p2;
    }

    public IDPair getIds() {
        return ids;
    }

    public void setIds(int id, int cid) {
        ids = new IDPair(id, cid);
    }


    public IDPair getPid1() {
        return pid1;
    }

    public IDPair getPid2() {
        return pid2;
    }
    
    public Wire copy() {
        Wire ret = new Wire(ids.getComponentID(), ids.getCircuitID());
        ret.connectP1(pid1.copy());
        ret.connectP2(pid2.copy());
        return ret;
    }
    
    public String toString() {
        return ids + " - " + pid1 + " - " + pid2;
    }
}