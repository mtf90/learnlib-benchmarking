package de.learnlib.benchmarking.exception;

public class ExperimentNotYetFinishedException extends RuntimeException {

    public static final String MSG = "The experiment has not yet finished.";

    public ExperimentNotYetFinishedException() {
        super(MSG);
    }
}
