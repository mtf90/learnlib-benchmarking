package de.learnlib.benchmarking.api.learner;

import java.io.IOException;
import java.util.List;

import de.learnlib.benchmarking.dto.HypothesisDTO;
import de.learnlib.benchmarking.dto.TransducerResponseDTO;
import io.avaje.http.api.Consumes;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.automatalib.exception.FormatException;
import org.jspecify.annotations.Nullable;

@Path("/learner/mealy")
public interface LearnerEndpointMealy {

    @Post("/queries/{id}")
    @Produces(statusCode = 200)
    @Tag(name = "learner")
    List<List<String>> answerQueries(long id, List<List<String>> queries);

    @Post("/counterexample/{id}")
    @Consumes("text/vnd.graphviz")
    @Produces(statusCode = 200)
    @Tag(name = "learner")
    @Nullable TransducerResponseDTO findCounterExample(long id, HypothesisDTO hypothesis)
            throws IOException, FormatException;
}
