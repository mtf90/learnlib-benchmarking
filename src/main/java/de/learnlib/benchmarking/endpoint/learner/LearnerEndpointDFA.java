package de.learnlib.benchmarking.endpoint.learner;

import java.io.IOException;
import java.util.List;

import de.learnlib.benchmarking.dto.QueryDTO;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.ModelType;
import de.learnlib.benchmarking.util.cache.ModelCacheDFA;
import io.avaje.http.api.Consumes;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import org.jspecify.annotations.Nullable;

@Controller
@Path("/learner/dfa")
public class LearnerEndpointDFA extends AbstractLearnerEndpoint<DFA<?, String>, Boolean, Boolean> {

    private final EntityManager entityManager;

    /**
     * Default constructor. We depend on Vex's {@link Context} to simulate a request-scoped lifecycle which gives us a
     * fresh entity manager for each request.
     *
     * @param ctx
     *         the context
     */
    public LearnerEndpointDFA(Context ctx, ModelCacheDFA modelCache) {
        super(modelCache);
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    @Override
    protected ModelType getModelType() {
        return ModelType.DFA;
    }

    @Override
    protected Boolean transformOutput(Boolean out) {
        return out;
    }

    @Override
    protected InputModelData<String, ? extends DFA<?, String>> parseHypothesis(String hypothesis)
            throws IOException, FormatException {
        return ModelCacheDFA.readDFA(hypothesis);
    }

    @Post("/queries/{id}")
    @Produces(statusCode = 200)
    public List<Boolean> answerQueries(long id, List<QueryDTO> queries) {
        final var experiment = entityManager.find(DBExperiment.class, id);
        return super.answerQueries(experiment, queries);
    }

    @Post("/counterexample/{id}")
    @Consumes("text/vnd.graphviz")
    @Produces(statusCode = 200)
    public @Nullable QueryDTO findCounterExample(long id, String hypothesis) throws IOException, FormatException {
        final var experiment = entityManager.find(DBExperiment.class, id);
        return super.findCounterExample(experiment, hypothesis);
    }
}
