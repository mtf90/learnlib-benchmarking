package de.learnlib.benchmarking.impl;

import de.learnlib.benchmarking.BenchmarkingModule;
import de.learnlib.benchmarking.exception.ExhaustiveCounterexampleException;
import de.learnlib.benchmarking.exception.TypeMismatchException;
import io.avaje.http.api.Controller;
import io.avaje.http.api.ExceptionHandler;
import io.avaje.http.api.MediaType;
import io.avaje.http.api.Produces;
import io.avaje.jex.http.Context;
import net.automatalib.exception.FormatException;

/**
 * We need one controller without any dependencies to enable the generation of our {@link BenchmarkingModule} which
 * kicks of avaje CDI. We use this controller for some general configuration (e.g., exception handling) as well.
 */
@Controller
public class Activation {

    @ExceptionHandler
    @Produces(value = MediaType.TEXT_PLAIN, statusCode = 400)
    public String exceptionCtx(TypeMismatchException ex, Context ctx) {
        return ex.getMessage();
    }

    @ExceptionHandler
    @Produces(value = MediaType.TEXT_PLAIN, statusCode = 404)
    public String exceptionCtx(ExhaustiveCounterexampleException ex, Context ctx) {
        return ex.getMessage();
    }

    @ExceptionHandler
    @Produces(value = MediaType.TEXT_PLAIN, statusCode = 400)
    public String exceptionCtx(FormatException ex, Context ctx) {
        return ex.getMessage();
    }
}
