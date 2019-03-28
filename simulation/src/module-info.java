import service.Evaluator;

module carsim.simulation {

	requires javafx.graphics;
	exports simulation to javafx.graphics;

	requires carsim.network;
	requires java.logging;

	provides Evaluator with simulation.SimEvaluator;
}