package simulation;

import network.Network;
import service.Evaluator;

public final class CarEvaluator implements Evaluator {
	@Override
	public double evaluate(Network network) {
		World.main();

		World world = World.getWorld();
		while (world == null)	// spin wait for non-null value
			world = World.getWorld();

		// TODO evaluate car progress

		final long operations = world.getOperations();

		final double progress = 0;

		return progress * progress / operations;
	}
}
