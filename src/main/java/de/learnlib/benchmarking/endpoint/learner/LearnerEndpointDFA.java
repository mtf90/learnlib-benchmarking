package de.learnlib.benchmarking.endpoint.learner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.learnlib.benchmarking.dto.QueryDTO;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.ModelType;
import de.learnlib.benchmarking.util.ModelCacheDFA;
import de.learnlib.benchmarking.util.SimCountOracle;
import io.avaje.http.api.Consumes;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.exception.FormatException;
import org.jspecify.annotations.Nullable;

@Controller
@Path("/learner/dfa")
public class LearnerEndpointDFA {

    private final EntityManager entityManager;

    private final ModelCacheDFA modelCache;

    /**
     * Default constructor. We depend on Vex's {@link Context} to simulate a request-scoped lifecycle which gives us a
     * fresh entity manager for each request.
     *
     * @param ctx
     *         the context
     */
    public LearnerEndpointDFA(Context ctx, ModelCacheDFA modelCache) {
        this.entityManager = ctx.attribute(EntityManager.class);
        this.modelCache = modelCache;
    }

    @Post("/queries/{id}")
    public List<Boolean> answerQueries(long id, List<QueryDTO> queries) {
        final var experiment = entityManager.find(DBExperiment.class, id);

        if (experiment.getType() != ModelType.DFA) {
            throw new IllegalArgumentException();
        }

        final var model = modelCache.readDFA(experiment);
        final var statistics = experiment.getStatistics();

        final int oldQueries = statistics.getNumberOfLearnerQueries();
        final int oldSymbols = statistics.getNumberOfLearnerSymbols();

        final int newQueries = queries.size();
        final List<Boolean> result = new ArrayList<>(newQueries);
        int newSymbols = 0;

        for (QueryDTO query : queries) {
            newSymbols += query.symbols().size();
            result.add(model.accepts(query.symbols()));
        }

        statistics.setNumberOfLearnerQueries(oldQueries + newQueries);
        statistics.setNumberOfLearnerSymbols(oldSymbols + newSymbols);

        return result;
    }

    @Post("/counterexample/{id}")
    @Consumes("text/vnd.graphviz")
    public @Nullable QueryDTO findCounterExample(long id, String hypothesis) throws IOException, FormatException {
        final var experiment = entityManager.find(DBExperiment.class, id);

        if (experiment.getType() != ModelType.DFA) {
            throw new IllegalArgumentException();
        }

        final var model = ModelCacheDFA.readDFA(hypothesis);
        final var hyp = model.model;
        final var alphabet = model.alphabet;

        if (alphabet.size() != experiment.getAlphabetSize()) {
            throw new IllegalArgumentException();
        }

        var mqo = new SimCountOracle<>(hyp);
        var eqo = modelCache.getEQO(mqo);

        var ce = eqo.findCounterExample(hyp, Alphabets.integers(0, alphabet.size()));

        final var statistics = experiment.getStatistics();
        final int oldQueries = statistics.getNumberOfTesterQueries();
        final int oldSymbols = statistics.getNumberOfTesterSymbols();
        final int newQueries = mqo.getNumberOfQueries();
        final int newSymbols = mqo.getNumberOfSymbols();

        statistics.setNumberOfTesterQueries(oldQueries + newQueries);
        statistics.setNumberOfLearnerSymbols(oldSymbols + newSymbols);

        if (ce == null) {
            return null;
        } else {
            return new QueryDTO(ce.getInput().asList());
        }
    }

}
