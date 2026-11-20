package de.learnlib.benchmarking.exception;

public class TypeMismatchException extends RuntimeException {

    public static String MSG = "Model type mismatch between endpoint and experiment";

    public TypeMismatchException() {
        super(MSG);
    }
}
