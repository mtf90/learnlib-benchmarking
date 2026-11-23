package de.learnlib.benchmarking.impl.tester;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import de.learnlib.benchmarking.entity.DBTesterExperiment;
import de.learnlib.benchmarking.exception.ExperimentNotFoundException;
import de.learnlib.benchmarking.exception.TypeMismatchException;
import de.learnlib.benchmarking.util.cache.AbstractModelService;
import de.learnlib.cli.option.Type;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractTesterEndpoint<M extends SuffixOutput<String, D>, D, OUT> {

    private final AbstractModelService<M, D> modelCache;

    public AbstractTesterEndpoint(AbstractModelService<M, D> modelCache) {
        this.modelCache = modelCache;
    }

    protected abstract Type getModelType();

    protected abstract OUT transformOutput(DefaultQuery<String, D> query);

    protected String getHypothesis(@Nullable DBTesterExperiment experiment) throws IOException {
        if (experiment == null) {
            throw new ExperimentNotFoundException();
        }

        return modelCache.getHypothesisAsString(experiment);
    }

    protected @Nullable OUT checkForCounterexamples(@Nullable DBTesterExperiment experiment,
                                                    List<List<String>> testQueries) throws IOException {

        if (experiment == null) {
            throw new ExperimentNotFoundException();
        }

        if (experiment.getType() != getModelType()) {
            throw new TypeMismatchException();
        }

        var hyp = modelCache.getHypothesis(experiment);
        var sul = modelCache.readFromPath(experiment);

        DefaultQuery<String, D> ce = null;
        int symCount = 0;
        int queryCount = 0;

        for (List<String> q : testQueries) {
            symCount += q.size();
            queryCount++;
            var hypOut = hyp.computeOutput(q);
            var sulOut = sul.computeOutput(q);

            if (!Objects.equals(hypOut, sulOut)) {
                ce = new DefaultQuery<>(Word.fromList(q), sulOut);
                break;
            }
        }

        var statistics = experiment.getStatistics();
        var oldTesterQueries = statistics.getNumberOfTesterQueries();
        var oldTesterSymbols = statistics.getNumberOfTesterSymbols();
        statistics.setNumberOfTesterQueries(oldTesterQueries + queryCount);
        statistics.setNumberOfTesterSymbols(oldTesterSymbols + symCount);

        if (ce == null) {
            return null;
        } else {
            modelCache.refineHypothesis(experiment, ce);
            return transformOutput(ce);
        }
    }
}
