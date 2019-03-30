package neat;

import network.Connection;
import service.Evolver;
import network.Network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class Neat implements Evolver {

	@Override
	public Collection<Network> initPopulation(int populationSize, int numInputs, int numOutputs) {
		// TODO implement this
		return null;
	}

	@Override
	public Network initNetwork(int numInputs, int numOutputs) {
		// TODO implement this
		return null;
	}

	@Override
	public Collection<Network> nextGeneration(
			Map<? extends Network, ? extends Double> prevGenToFitness,
			int nextGenSize,
			double harshness) {
		// TODO implement this
		return null;
	}



	/**
	 * Performs a crossover using the specified Networks and returns the offspring.
	 */
	public Network reproduce(Network parent1, Network parent2) {	// more than 2 parents?

//		final Network child = new Network();
		// TODO implement this
		final Network child = null;

//		final Network fittest;
//		// if of equal fitness, choose a random one
//		if (this.getFitness() == other.getFitness())
//			fittest = randomBoolean() ? this : other;
//		else
//			fittest = this.getFitness() > other.getFitness() ? this : other;

		final Map<Long, Connection> connects1 = parent1.getInnovNumToConnections();
		final Map<Long, Connection> connects2 = parent2.getInnovNumToConnections();


		final List<Long> innovNums1 = new ArrayList<>(connects1.keySet());
		final List<Long> innovNums2 = new ArrayList<>(connects2.keySet());

		int i1 = 0, i2 = 0;
		for ( ; i1 < innovNums1.size() && i2 < innovNums2.size(); ) {
			final long innovNum1 = innovNums1.get(i1);
			final long innovNum2 = innovNums2.get(i2);

			// add matching genes randomly
			if (innovNum1 == innovNum2) {
				child.putConnection(
						(Math.random() > 0.5) ?
								connects1.get(innovNum1) :
								connects2.get(innovNum2)
				);
				i1++; i2++;
			}
			else {	// add all disjoint genes
				final Connection c;
				if (innovNum1 < innovNum2) {
					c = connects1.get(innovNum1); i1++;
				}
				else {
					c = connects2.get(innovNum2); i2++;
				}

				child.putConnection(c);
			}
		}

		// add all excess genes
		for ( ; i1 < innovNums1.size(); i1++)
			child.putConnection(connects1.get(innovNums1.get(i1)));
		for ( ; i2 < innovNums2.size(); i2++)
			child.putConnection(connects2.get(innovNums2.get(i2)));

		return child;
	}
}
