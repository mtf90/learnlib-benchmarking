package de.learnlib.benchmarking.impl.learner;

import java.io.IOException;
import java.util.List;

import de.learnlib.benchmarking.api.learner.LearnerEndpointDFA;
import de.learnlib.benchmarking.dto.AcceptorResponseDTO;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.DBLearnerExperiment;
import de.learnlib.benchmarking.util.cache.ModelCacheDFA;
import de.learnlib.cli.option.Type;
import de.learnlib.query.DefaultQuery;
import io.avaje.http.api.Controller;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import org.checkerframework.checker.nullness.qual.Nullable;

@Controller
public class LearnerEndpointDFAImpl
        extends AbstractLearnerEndpoint<DFA<?, String>, Boolean, Boolean, AcceptorResponseDTO>
        implements LearnerEndpointDFA {

    private final EntityManager entityManager;

    /**
     * Default constructor. We depend on Vex's {@link Context} to simulate a request-scoped lifecycle which gives us a
     * fresh entity manager for each request.
     *
     * @param ctx
     *         the context
     */
    public LearnerEndpointDFAImpl(Context ctx, ModelCacheDFA modelCache) {
        super(modelCache);
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    @Override
    protected Type getModelType() {
        return Type.DFA;
    }

    @Override
    protected Boolean transformMQOutput(Boolean out) {
        return out;
    }

    @Override
    protected AcceptorResponseDTO transformEQOutput(DefaultQuery<String, Boolean> out) {
        return new AcceptorResponseDTO(out.getInput().asList(), out.getOutput());
    }

    @Override
    protected InputModelData<String, ? extends DFA<?, String>> parseHypothesis(String hypothesis)
            throws IOException, FormatException {
        return super.modelCache.readFromDot(hypothesis);
    }

    @Override
    public List<Boolean> answerQueries(long id, List<List<String>> queries) {
        return super.answerQueries(entityManager.find(DBLearnerExperiment.class, id), queries);
    }

    @Override
    public @Nullable AcceptorResponseDTO findCounterExample(long id, String hypothesis)
            throws IOException, FormatException {
        return super.findCounterExample(entityManager.find(DBLearnerExperiment.class, id), hypothesis);
    }
}
