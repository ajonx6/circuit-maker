package circuit.components;

import java.util.Objects;

// Represents a unique id relative to the components
public class IDPair {
    // Represents the id of the component within the current circuit
    // This stays the same when added to a parent circuit
    private int componentID;
    // Represents the id of the circuit this component is in
    // This is updated when added to a parent circuit
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

    public String toString() {
        // return "id=" + componentID + ", cid=" + circuitID;
        return componentID + "/" + circuitID;
    }
}