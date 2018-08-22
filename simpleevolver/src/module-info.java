import service.Evolver;

module carsim.simpleevolver {
	requires carsim.network;

	provides Evolver with simpleevolver.SimpleEvolver;
}