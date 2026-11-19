package de.learnlib.benchmarking.impl.tester;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.learnlib.benchmarking.api.tester.TesterEndpointDFA;
import de.learnlib.benchmarking.api.tester.TesterEndpointMealy;
import de.learnlib.benchmarking.dto.AcceptorResponseDTO;
import de.learnlib.benchmarking.dto.TransducerResponseDTO;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.util.cache.ModelCacheDFA;
import de.learnlib.benchmarking.util.cache.ModelCacheMealy;
import de.learnlib.benchmarking.util.runner.RunnerCacheDFA;
import de.learnlib.benchmarking.util.runner.RunnerCacheMealy;
import de.learnlib.cli.option.Type;
import de.learnlib.query.DefaultQuery;
import io.avaje.http.api.Controller;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

@Controller
public class TesterEndpointMealyImpl
        extends AbstractTesterEndpoint<MealyMachine<?, String, ?, String>, Word<String>, TransducerResponseDTO>
        implements TesterEndpointMealy {

    private final EntityManager entityManager;

    public TesterEndpointMealyImpl(Context ctx, RunnerCacheMealy runnerCacheDFA, ModelCacheMealy modelCacheDFA) {
        super(runnerCacheDFA, modelCacheDFA);
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    @Override
    protected Type getModelType() {
        return Type.MEALY;
    }

    @Override
    protected void writeHypothesis(MealyMachine<?, String, ?, String> hypothesis, Collection<String> alphabet, Writer w)
            throws IOException {
        GraphDOT.write(hypothesis, alphabet, w);
    }

    @Override
    protected TransducerResponseDTO transformOutput(DefaultQuery<String, Word<String>> query) {
        return new TransducerResponseDTO(query.getInput().asList(), query.getOutput().asList());
    }

    @Override
    public String getHypothesis(long id) throws ExecutionException, InterruptedException, IOException {
        return super.getHypothesis(entityManager.find(DBExperiment.class, id));
    }

    @Override
    public @Nullable TransducerResponseDTO checkForCounterexamples(long id, List<List<String>> testQueries)
            throws ExecutionException, InterruptedException {
        return super.checkForCounterexamples(entityManager.find(DBExperiment.class, id), testQueries);
    }
}
