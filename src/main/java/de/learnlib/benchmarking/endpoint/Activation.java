package de.learnlib.benchmarking.endpoint;

import de.learnlib.benchmarking.BenchmarkingModule;
import io.avaje.http.api.Controller;
import io.avaje.http.api.ExceptionHandler;
import io.avaje.http.api.MediaType;
import io.avaje.http.api.Produces;
import io.avaje.jex.http.Context;

/**
 * We need one controller without any dependencies to enable the generation of our {@link BenchmarkingModule} which
 * kicks of avaje CDI. We use this controller for some general configuration (e.g., exception handling) as well.
 */
@Controller
public class Activation {

    @ExceptionHandler
    @Produces(value = MediaType.TEXT_PLAIN, statusCode = 400)
    public String exceptionCtx(IllegalArgumentException ex, Context ctx) {
        return ex.getMessage();
    }
}
