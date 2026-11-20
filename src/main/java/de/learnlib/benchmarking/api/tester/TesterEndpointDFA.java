package de.learnlib.benchmarking.api.tester;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.learnlib.benchmarking.dto.AcceptorResponseDTO;
import io.avaje.http.api.Get;
import io.avaje.http.api.OpenAPIResponse;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.checkerframework.checker.nullness.qual.Nullable;

@Path("/tester/dfa")
public interface TesterEndpointDFA {

    @Get("hypothesis/{id}")
    @Produces("text/vnd.graphviz")
    @Tag(name = "tester")
    String getHypothesis(long id) throws ExecutionException, InterruptedException, IOException;

    @Post("/counterexample/{id}")
    @Tag(name = "tester")
    @Nullable AcceptorResponseDTO checkForCounterexamples(long id, List<List<String>> testQueries)
            throws ExecutionException, InterruptedException, IOException;

}
