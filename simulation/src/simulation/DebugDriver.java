package simulation;

/**
 * A dummy class for creating dummy debug objects.
 */
final class DebugDriver extends Driver {
	DebugDriver() {
		super(null, null, null);
	}

	// override methods to make them do nothing
	@Override void setOperations(final long operations) {}
	@Override void setDistance(double distance) {}
	@Override double getDistance() { return 0; }
	@Override long getOperations() { return 0; }
	@Override double getEvaluation() { return 0; }
	@Override void drive() {}

	@Override
	public String toString() {
		return "Debug Driver";
	}
}
