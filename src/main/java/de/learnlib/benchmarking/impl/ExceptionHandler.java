package de.learnlib.benchmarking.impl;

import de.learnlib.benchmarking.exception.ExperimentNotFoundException;
import de.learnlib.benchmarking.exception.ExperimentNotYetFinishedException;
import de.learnlib.benchmarking.exception.ExperimentTerminatedException;
import de.learnlib.benchmarking.exception.TypeMismatchException;
import io.avaje.http.api.Controller;
import io.avaje.http.api.MediaType;
import io.avaje.http.api.Produces;
import jakarta.inject.Singleton;
import net.automatalib.exception.FormatException;

/**
 * Transform exceptions into HTTP response codes.
 */
@Controller
@Singleton
public class ExceptionHandler {

    @io.avaje.http.api.ExceptionHandler
    @Produces(value = MediaType.TEXT_PLAIN, statusCode = 400)
    public String exceptionCtx(TypeMismatchException ex) {
        return ex.getMessage();
    }

    @io.avaje.http.api.ExceptionHandler
    @Produces(value = MediaType.TEXT_PLAIN, statusCode = 404)
    public String exceptionCtx(ExperimentTerminatedException ex) {
        return ex.getMessage();
    }

    @io.avaje.http.api.ExceptionHandler
    @Produces(value = MediaType.TEXT_PLAIN, statusCode = 404)
    public String exceptionCtx(ExperimentNotFoundException ex) {
        return ex.getMessage();
    }

    @io.avaje.http.api.ExceptionHandler
    @Produces(value = MediaType.TEXT_PLAIN, statusCode = 409)
    public String exceptionCtx(ExperimentNotYetFinishedException ex) {
        return ex.getMessage();
    }

    @io.avaje.http.api.ExceptionHandler
    @Produces(value = MediaType.TEXT_PLAIN, statusCode = 400)
    public String exceptionCtx(FormatException ex) {
        return ex.getMessage();
    }
}
