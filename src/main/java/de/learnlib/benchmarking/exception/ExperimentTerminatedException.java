package de.learnlib.benchmarking.exception;

public class ExperimentTerminatedException extends RuntimeException {

    public ExperimentTerminatedException() {
        super("The experiment has already run to completion");
    }
}
