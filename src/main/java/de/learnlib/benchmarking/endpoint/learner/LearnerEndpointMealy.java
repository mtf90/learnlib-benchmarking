package de.learnlib.benchmarking.endpoint.learner;

import java.io.IOException;
import java.util.List;

import de.learnlib.benchmarking.dto.QueryDTO;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.ModelType;
import de.learnlib.benchmarking.util.cache.ModelCacheMealy;
import io.avaje.http.api.Consumes;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.word.Word;
import org.jspecify.annotations.Nullable;

@Controller
@Path("/learner/mealy")
public class LearnerEndpointMealy
        extends AbstractLearnerEndpoint<MealyMachine<?, String, ?, String>, Word<String>, QueryDTO> {

    private final EntityManager entityManager;

    /**
     * Default constructor. We depend on Vex's {@link Context} to simulate a request-scoped lifecycle which gives us a
     * fresh entity manager for each request.
     *
     * @param ctx
     *         the context
     */
    public LearnerEndpointMealy(Context ctx, ModelCacheMealy modelCache) {
        super(modelCache);
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    @Override
    protected ModelType getModelType() {
        return ModelType.MEALY;
    }

    @Override
    protected QueryDTO transformOutput(Word<String> out) {
        return new QueryDTO(out.asList());
    }

    @Override
    protected InputModelData<String, ? extends MealyMachine<?, String, ?, String>> parseHypothesis(String hypothesis)
            throws IOException, FormatException {
        return ModelCacheMealy.readMealy(hypothesis);
    }

    @Post("/queries/{id}")
    @Produces(statusCode = 200)
    public List<QueryDTO> answerQueries(long id, List<QueryDTO> queries) {
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
