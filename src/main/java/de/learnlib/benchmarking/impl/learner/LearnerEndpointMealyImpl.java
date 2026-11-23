package de.learnlib.benchmarking.impl.learner;

import java.io.IOException;
import java.util.List;

import de.learnlib.benchmarking.api.learner.LearnerEndpointMealy;
import de.learnlib.benchmarking.dto.TransducerResponseDTO;
import de.learnlib.benchmarking.entity.DBLearnerExperiment;
import de.learnlib.benchmarking.util.cache.ModelServiceMealy;
import de.learnlib.cli.option.Type;
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
    public LearnerEndpointMealyImpl(Context ctx, ModelServiceMealy modelCache) {
        super(modelCache);
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    @Override
    protected Type getModelType() {
        return Type.MEALY;
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
        return super.modelService.readFromDot(hypothesis);
    }

    @Override
    public List<List<String>> answerQueries(long id, List<List<String>> queries) {
        return super.answerQueries(entityManager.find(DBLearnerExperiment.class, id), queries);
    }

    @Override
    public @Nullable TransducerResponseDTO findCounterExample(long id, String hypothesis)
            throws IOException, FormatException {
        return super.findCounterExample(entityManager.find(DBLearnerExperiment.class, id), hypothesis);
    }
}
