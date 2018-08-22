package network;

public final class EvaluatedNetwork {
	private final Network network;
	private final double fitness;

	public EvaluatedNetwork(Network network, double fitness) {
		this.network = network;
		this.fitness = fitness;
	}

	public double getFitness() { return fitness; }
	public Network getNetwork() { return network; }
}
