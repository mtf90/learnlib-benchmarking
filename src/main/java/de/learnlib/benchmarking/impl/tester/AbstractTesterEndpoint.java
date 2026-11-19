package de.learnlib.benchmarking.impl.tester;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.exception.ExhaustiveCounterexampleException;
import de.learnlib.benchmarking.exception.TypeMismatchException;
import de.learnlib.benchmarking.util.cache.AbstractModelCache;
import de.learnlib.benchmarking.util.runner.RunnerCache;
import de.learnlib.cli.option.Type;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractTesterEndpoint<M extends SuffixOutput<String, D>, D, OUT> {

    private final RunnerCache<M, D> runnerCache;
    private final AbstractModelCache<M, String, D> modelCache;

    public AbstractTesterEndpoint(RunnerCache<M, D> runnerCache, AbstractModelCache<M, String, D> modelCache) {
        this.runnerCache = runnerCache;
        this.modelCache = modelCache;
    }

    protected abstract Type getModelType();

    protected abstract void writeHypothesis(M hypothesis, Collection<String> alphabet, Writer w) throws IOException;

    protected abstract OUT transformOutput(DefaultQuery<String, D> query);

    protected String getHypothesis(DBExperiment experiment)
            throws ExecutionException, InterruptedException, IOException {

        if (experiment.getType() != getModelType()) {
            throw new TypeMismatchException();
        }
        
        final var runner = runnerCache.getRunner(experiment.getId());
        final var hyp = runner.getHypothesis();

        try (Writer w = new StringWriter()) {
            writeHypothesis(hyp, experiment.getAlphabet(), w);
            return w.toString();
        }
    }

    protected @Nullable OUT checkForCounterexamples(DBExperiment experiment, List<List<String>> testQueries)
            throws ExecutionException, InterruptedException {

        if (experiment.getType() != getModelType()) {
            throw new TypeMismatchException();
        }

        final var runner = runnerCache.getRunner(experiment.getId());

        if (runner == null) {
            throw new ExhaustiveCounterexampleException();
        }

        var hyp = runner.getHypothesis();
        var sul = modelCache.readFromPath(experiment);

        DefaultQuery<String, D> ce = null;
        int symCount = 0;

        for (List<String> q : testQueries) {
            symCount += q.size();
            var hypOut = hyp.computeOutput(q);
            var sulOut = sul.computeOutput(q);

            if (ce == null && !Objects.equals(hypOut, sulOut)) {
                ce = new DefaultQuery<>(Word.fromList(q), sulOut);
            }
        }

        var statistics = experiment.getStatistics();
        var oldTesterQueries = statistics.getNumberOfTesterQueries();
        var oldTesterSymbols = statistics.getNumberOfTesterSymbols();
        statistics.setNumberOfTesterQueries(oldTesterQueries + testQueries.size());
        statistics.setNumberOfTesterSymbols(oldTesterSymbols + symCount);

        if (ce == null) {
            return null;
        } else {
            runner.refine(ce);
            return transformOutput(ce);
        }
    }
}
