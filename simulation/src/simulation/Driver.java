package simulation;

import network.Network;

/**
 * Represents a Driver of a Car in the World. In addition to a reference to a Network
 * instance used to control the Car, instances of this class also record the number of
 * operations used and the degree of completion achieved.
 */
final class Driver implements Comparable<Driver> {
	private final Track track;
	private final Car car;
	private final Network network;

	/** number of operations the network used */
	private volatile long operations = -1;
	/** degree of completion; a number from 0 to 1 describing how much of the track was
	 * completed */
	private volatile double completion = -1;

	/** A cached result of {@link SimEvaluator#evaluateDriver} */
	private volatile double eval = -1;


	/**
	 * Constructs a new Driver controlled by the specified Network driving the specified
	 * Car in the specified Track.
	 */
	Driver(final Track track, final Car car, final Network network) {
		this.track = track;
		this.car = car;
		this.network = network;
	}


	synchronized void setOperations(final long operations) {
		this.operations = operations;
	}

	synchronized void setCompletion(double completion) {
		this.completion = completion;
	}

	Track getTrack() { return track; }
	Network getNetwork() { return network; }
	Car getCar() { return car; }
	double getCompletion() { return completion; }
	long getOperations() { return operations; }

	synchronized double getEvaluation() {
		if (eval < 0)
			eval = SimEvaluator.evaluateDriver(this);
		return eval;
	}


	@Override
	public int compareTo(final Driver other) {
		return Double.compare(this.getEvaluation(), other.getEvaluation());
	}


	/* ****************************************
	Interface for Network to control Car.
	******************************************/

	void drive() {
		// get response from network
		final var inputs = car.getRangeReadingsAsList(track.getEdges());
		final var outputs = network.compute(inputs);

		// apply network's response to car
		int i = 0, outputSize = outputs.size();
		// set value to true if network output greater than 0, otherwise false
		car.setIsAccelerating(  (i < outputSize) && (outputs.get(i++) > 0) );
		car.setIsDecelerating(  (i < outputSize) && (outputs.get(i++) > 0) );
		car.setIsBraking(       (i < outputSize) && (outputs.get(i++) > 0) );
		car.setIsTurningLeft(   (i < outputSize) && (outputs.get(i++) > 0) );
		car.setIsTurningRight(  (i < outputSize) && (outputs.get(i) > 0) );
	}
}
