package de.learnlib.benchmarking.impl.tester;

import java.io.IOException;
import java.util.List;

import de.learnlib.benchmarking.api.TesterEndpointDFA;
import de.learnlib.benchmarking.dto.AcceptorResponseDTO;
import de.learnlib.benchmarking.entity.DBTesterExperiment;
import de.learnlib.benchmarking.util.cache.ModelServiceDFA;
import de.learnlib.cli.option.Type;
import de.learnlib.query.DefaultQuery;
import io.avaje.http.api.Controller;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;
import net.automatalib.automaton.fsa.DFA;
import org.checkerframework.checker.nullness.qual.Nullable;

@Controller
public class TesterEndpointDFAImpl extends AbstractTesterEndpoint<DFA<?, String>, Boolean, AcceptorResponseDTO>
        implements TesterEndpointDFA {

    private final EntityManager entityManager;

    public TesterEndpointDFAImpl(Context ctx, ModelServiceDFA modelServiceDFA) {
        super(modelServiceDFA);
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    @Override
    protected Type getModelType() {
        return Type.DFA;
    }

    @Override
    protected AcceptorResponseDTO transformOutput(DefaultQuery<String, Boolean> query) {
        return new AcceptorResponseDTO(query);
    }

    @Override
    public String getHypothesis(long id) throws IOException {
        return super.getHypothesis(entityManager.find(DBTesterExperiment.class, id));
    }

    @Override
    public @Nullable AcceptorResponseDTO checkForCounterexamples(long id, List<List<String>> testQueries)
            throws IOException {
        return super.checkForCounterexamples(entityManager.find(DBTesterExperiment.class, id), testQueries);
    }
}
