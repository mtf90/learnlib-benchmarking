package de.learnlib.benchmarking.impl.tester;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.learnlib.benchmarking.entity.DBTesterExperiment;
import de.learnlib.benchmarking.exception.ExperimentNotFoundException;
import de.learnlib.benchmarking.exception.ExperimentTerminatedException;
import de.learnlib.benchmarking.exception.TypeMismatchException;
import de.learnlib.benchmarking.util.cache.AbstractModelCache;
import de.learnlib.benchmarking.util.runner.RunnerCache;
import de.learnlib.cli.option.Type;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.concept.SuffixOutput;
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

    protected abstract boolean testEquivalence(M hyp, M model, Collection<String> inputs);

    protected String getHypothesis(@Nullable DBTesterExperiment experiment)
            throws ExecutionException, InterruptedException, IOException {

        if (experiment == null) {
            throw new ExperimentNotFoundException();
        }

        if (experiment.getType() != getModelType()) {
            throw new TypeMismatchException();
        }

        var finalHyp = experiment.getFinalHypothesis();

        if (finalHyp == null) {
            final var runner = runnerCache.getRunner(experiment.getId());
            final var hyp = runner.getHypothesis();

            return hypToString(hyp, experiment.getAlphabet());
        } else {
            return finalHyp;
        }
    }

    protected @Nullable OUT checkForCounterexamples(@Nullable DBTesterExperiment experiment, List<List<String>> testQueries)
            throws ExecutionException, InterruptedException, IOException {

        if (experiment == null) {
            throw new ExperimentNotFoundException();
        }

        if (experiment.getType() != getModelType()) {
            throw new TypeMismatchException();
        }

        final var runner = runnerCache.getRunner(experiment.getId());

        if (runner == null) {
            throw new ExperimentTerminatedException();
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
            runner.refine(experiment, ce);
            var nextHyp = runner.getHypothesis();
            var equiv = testEquivalence(sul, nextHyp, experiment.getAlphabet());

            if (equiv) {
                runnerCache.removeRunner(experiment.getId());
                experiment.setFinalHypothesis(hypToString(nextHyp, experiment.getAlphabet()));
            }

            return transformOutput(ce);
        }
    }

    private String hypToString(M hyp, Collection<String> alphabet) throws IOException {
        try (Writer w = new StringWriter()) {
            writeHypothesis(hyp, alphabet, w);
            return w.toString();
        }
    }
}
