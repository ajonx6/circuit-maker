package circuit.util;

public class IDAssigner {
	public int baseID;

	public IDAssigner() {
		this(0);
	}

	public IDAssigner(int baseID) {
		this.baseID = baseID;
	}

	public int nextID() {
		return baseID++;
	}

	public int getCurrentID() {
		return baseID;
	}
}