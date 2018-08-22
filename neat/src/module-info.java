import service.Evolver;

/**
 * This module provides an algorithm that selects, improves, and reproduces new neural
 * networks in each generation.
 */
module carsim.neat {
	requires carsim.network;

	provides Evolver with neat.Neat;
}