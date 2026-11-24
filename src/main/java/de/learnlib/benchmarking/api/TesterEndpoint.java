package de.learnlib.benchmarking.api;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.avaje.http.api.Get;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface TesterEndpoint<CEOUT> {

    /**
     * Returns the current hypothesis of the given experiment.
     *
     * @param id
     *         the experiment id
     *
     * @return the current hypothesis (encoded as a .dot file)
     */
    @Get("hypothesis/{id}")
    @Produces("text/vnd.graphviz")
    @Tag(name = "tester")
    String getHypothesis(long id) throws ExecutionException, InterruptedException, IOException;

    /**
     * Checks whether the provided test queries contain a counterexample for the current hypothesis and the SUL of the
     * given experiment. In case a counterexample is found, this call block until the subsequent hypothesis refinement
     * has finished. This means that after this call returns, a fresh hypothesis can be directly obtained. Only test
     * queries up until a counterexample has been found will be counted towards the experiment statistics.
     *
     * @param id
     *         the experiment id
     * @param testQueries
     *         the queries to check
     *
     * @return the test query (including the SUL's response) if a counterexample has been found, {@code null} otherwise
     */
    @Post("/counterexample/{id}")
    @Tag(name = "tester")
    @Nullable
    CEOUT checkForCounterexamples(long id, List<List<String>> testQueries)
            throws ExecutionException, InterruptedException, IOException;

}
