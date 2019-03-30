import service.Evolver;

module carsim.simpleevolver {
	requires carsim.network;
	requires util.simple.logging;

	provides Evolver with simpleevolver.SimpleEvolver;
}