package de.learnlib.benchmarking.exception;

public class ExperimentNotFoundException extends RuntimeException {

    public static String MSG = "No (type-specific) experiment with the given id has been found.";

    public ExperimentNotFoundException() {
        super(MSG);
    }
}
