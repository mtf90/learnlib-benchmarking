package de.learnlib.benchmarking.exception;

/**
 * Thrown when one is testing for a counterexample but the hypothesis has already been learned to completion.
 */
public class ExhaustiveCounterexampleException extends RuntimeException {

    public ExhaustiveCounterexampleException() {
        super("The experiment has already run to completion");
    }
}
