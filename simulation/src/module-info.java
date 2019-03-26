import service.Evaluator;

module carsim.simulation {

	requires javafx.graphics;
	exports simulation to javafx.graphics;

	requires carsim.network;

	provides Evaluator with simulation.SimEvaluator;
}

// IntelliJ just refuses to compile this for some reason. Manual javac works fine.