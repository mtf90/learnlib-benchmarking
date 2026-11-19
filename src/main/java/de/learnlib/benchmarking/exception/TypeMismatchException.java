package de.learnlib.benchmarking.exception;

public class TypeMismatchException extends RuntimeException {

    public TypeMismatchException() {
        super("Type mismatch between endpoint and experiment");
    }
}
