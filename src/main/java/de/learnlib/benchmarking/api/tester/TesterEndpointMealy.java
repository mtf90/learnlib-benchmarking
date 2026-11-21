package de.learnlib.benchmarking.api.tester;

import java.io.IOException;
import java.util.List;

import de.learnlib.benchmarking.dto.TransducerResponseDTO;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.checkerframework.checker.nullness.qual.Nullable;

@Path("/tester/mealy")
public interface TesterEndpointMealy {

    @Get("hypothesis/{id}")
    @Produces("text/vnd.graphviz")
    @Tag(name = "tester")
    String getHypothesis(long id) throws IOException;

    @Post("/counterexample/{id}")
    @Tag(name = "tester")
    @Nullable
    TransducerResponseDTO checkForCounterexamples(long id, List<List<String>> testQueries) throws IOException;

}
