package circuit.components;

import java.util.Objects;

public class IDPair {
    private int componentID;
    private int circuitID;

    public IDPair(int componentID, int circuitID) {
        this.componentID = componentID;
        this.circuitID = circuitID;
    }

    public int getComponentID() {
        return componentID;
    }

    public void setComponentID(int componentID) {
        this.componentID = componentID;
    }

    public int getCircuitID() {
        return circuitID;
    }

    public void setCircuitID(int circuitID) {
        this.circuitID = circuitID;
    }
    
    public String toString() {
        // return "id=" + componentID + ", cid=" + circuitID;
        return componentID + "/" + circuitID;
    }
    
    public IDPair copy() {
        return new IDPair(componentID, circuitID);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IDPair idPair = (IDPair) o;
        return componentID == idPair.componentID && circuitID == idPair.circuitID;
    }

    public int hashCode() {
        return Objects.hash(componentID, circuitID);
    }
}