package de.learnlib.benchmarking.impl.tester;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.learnlib.benchmarking.api.tester.TesterEndpointDFA;
import de.learnlib.benchmarking.dto.AcceptorResponseDTO;
import de.learnlib.benchmarking.entity.DBTesterExperiment;
import de.learnlib.benchmarking.util.cache.ModelCacheDFA;
import de.learnlib.benchmarking.util.runner.RunnerCacheDFA;
import de.learnlib.cli.option.Type;
import de.learnlib.query.DefaultQuery;
import io.avaje.http.api.Controller;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.Automata;
import org.checkerframework.checker.nullness.qual.Nullable;

@Controller
public class TesterEndpointDFAImpl extends AbstractTesterEndpoint<DFA<?, String>, Boolean, AcceptorResponseDTO>
        implements TesterEndpointDFA {

    private final EntityManager entityManager;

    public TesterEndpointDFAImpl(Context ctx, RunnerCacheDFA runnerCacheDFA, ModelCacheDFA modelCacheDFA) {
        super(runnerCacheDFA, modelCacheDFA);
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    @Override
    protected Type getModelType() {
        return Type.DFA;
    }

    @Override
    protected void writeHypothesis(DFA<?, String> hypothesis, Collection<String> alphabet, Writer w)
            throws IOException {
        GraphDOT.write(hypothesis, alphabet, w);
    }

    @Override
    protected AcceptorResponseDTO transformOutput(DefaultQuery<String, Boolean> query) {
        return new AcceptorResponseDTO(query);
    }

    @Override
    protected boolean testEquivalence(DFA<?, String> hyp, DFA<?, String> model, Collection<String> inputs) {
        return Automata.testEquivalence(hyp, model, inputs);
    }

    @Override
    public String getHypothesis(long id) throws ExecutionException, InterruptedException, IOException {
        return super.getHypothesis(entityManager.find(DBTesterExperiment.class, id));
    }

    @Override
    public @Nullable AcceptorResponseDTO checkForCounterexamples(long id, List<List<String>> testQueries)
            throws ExecutionException, InterruptedException, IOException {
        return super.checkForCounterexamples(entityManager.find(DBTesterExperiment.class, id), testQueries);
    }
}
