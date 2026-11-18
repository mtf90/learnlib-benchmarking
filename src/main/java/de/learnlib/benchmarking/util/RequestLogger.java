package de.learnlib.benchmarking.util;

import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestLogger implements HttpFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogger.class);

    @Override
    public void filter(Context ctx, FilterChain chain) {
        final long startNanos = System.nanoTime();
        try {
            chain.proceed();
        } finally {
            long duration = (System.nanoTime() - startNanos) / 1_000_000;
            LOGGER.debug("{} request to '{}' returned with status '{}' after {}ms",
                         ctx.method(),
                         ctx.path(),
                         ctx.status(),
                         duration);
        }
    }
}
