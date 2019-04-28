import service.Evaluator;
import service.Evolver;

module xor {
	provides Evaluator with xor.XorTest;
	provides Evolver with xor.XorEvolver;

	requires carsim.network;
}