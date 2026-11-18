package de.learnlib.benchmarking.impl.learner;

import java.io.IOException;
import java.util.List;

import de.learnlib.benchmarking.api.learner.LearnerEndpointMealy;
import de.learnlib.benchmarking.dto.HypothesisDTO;
import de.learnlib.benchmarking.dto.TransducerResponseDTO;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.ModelType;
import de.learnlib.benchmarking.util.cache.ModelCacheMealy;
import de.learnlib.query.DefaultQuery;
import io.avaje.http.api.Controller;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.word.Word;
import org.jspecify.annotations.Nullable;

@Controller
public class LearnerEndpointMealyImpl
        extends AbstractLearnerEndpoint<MealyMachine<?, String, ?, String>, Word<String>, List<String>, TransducerResponseDTO>
        implements LearnerEndpointMealy {

    private final EntityManager entityManager;

    /**
     * Default constructor. We depend on Vex's {@link Context} to simulate a request-scoped lifecycle which gives us a
     * fresh entity manager for each request.
     *
     * @param ctx
     *         the context
     */
    public LearnerEndpointMealyImpl(Context ctx, ModelCacheMealy modelCache) {
        super(modelCache);
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    @Override
    protected ModelType getModelType() {
        return ModelType.MEALY;
    }

    @Override
    protected List<String> transformMQOutput(Word<String> out) {
        return out.asList();
    }

    @Override
    protected TransducerResponseDTO transformEQOutput(DefaultQuery<String, Word<String>> out) {
        return new TransducerResponseDTO(out.getInput().asList(), out.getOutput().asList());
    }

    @Override
    protected InputModelData<String, ? extends MealyMachine<?, String, ?, String>> parseHypothesis(String hypothesis)
            throws IOException, FormatException {
        return super.modelCache.readFromDot(hypothesis);
    }

    @Override
    public List<List<String>> answerQueries(long id, List<List<String>> queries) {
        final var experiment = entityManager.find(DBExperiment.class, id);
        return super.answerQueries(experiment, queries);
    }

    @Override
    public @Nullable TransducerResponseDTO findCounterExample(long id, HypothesisDTO hypothesis)
            throws IOException, FormatException {
        final var experiment = entityManager.find(DBExperiment.class, id);
        return super.findCounterExample(experiment, hypothesis);
    }
}
