import service.Evaluator;
import service.Evolver;

module carsim.simulation {

	requires javafx.graphics;
	exports simulation to javafx.graphics;

	requires carsim.network;

	provides Evaluator with simulation.SimEvaluator;
	provides Evolver with simulation.SimEvolver;

	requires util.simple.logging;
}