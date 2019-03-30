import service.Evaluator;

module carsim.simulation {

	requires javafx.graphics;
	exports simulation to javafx.graphics;

	requires carsim.network;

	provides Evaluator with simulation.SimEvaluator;

	requires util.simple.logging;
}