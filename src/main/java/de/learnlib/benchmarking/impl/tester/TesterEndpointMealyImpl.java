package de.learnlib.benchmarking.impl.tester;

import java.io.IOException;
import java.util.List;

import de.learnlib.benchmarking.api.tester.TesterEndpointMealy;
import de.learnlib.benchmarking.dto.TransducerResponseDTO;
import de.learnlib.benchmarking.entity.DBTesterExperiment;
import de.learnlib.benchmarking.util.cache.ModelServiceMealy;
import de.learnlib.cli.option.Type;
import de.learnlib.query.DefaultQuery;
import io.avaje.http.api.Controller;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

@Controller
public class TesterEndpointMealyImpl
        extends AbstractTesterEndpoint<MealyMachine<?, String, ?, String>, Word<String>, TransducerResponseDTO>
        implements TesterEndpointMealy {

    private final EntityManager entityManager;

    public TesterEndpointMealyImpl(Context ctx, ModelServiceMealy modelCacheDFA) {
        super(modelCacheDFA);
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    @Override
    protected Type getModelType() {
        return Type.MEALY;
    }

    @Override
    protected TransducerResponseDTO transformOutput(DefaultQuery<String, Word<String>> query) {
        return new TransducerResponseDTO(query.getInput().asList(), query.getOutput().asList());
    }

    @Override
    public String getHypothesis(long id) throws IOException {
        return super.getHypothesis(entityManager.find(DBTesterExperiment.class, id));
    }

    @Override
    public @Nullable TransducerResponseDTO checkForCounterexamples(long id, List<List<String>> testQueries)
            throws IOException {
        return super.checkForCounterexamples(entityManager.find(DBTesterExperiment.class, id), testQueries);
    }
}
