import service.Evaluator;
import service.Evolver;

module carsim.network {

	exports network;
	exports service;
	exports util;

	uses Evaluator;
	uses Evolver;

	requires util.simple.logging;
}