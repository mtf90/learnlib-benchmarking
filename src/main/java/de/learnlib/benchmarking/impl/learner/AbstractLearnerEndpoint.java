package de.learnlib.benchmarking.impl.learner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.learnlib.benchmarking.entity.DBLearnerExperiment;
import de.learnlib.benchmarking.exception.ExperimentNotFoundException;
import de.learnlib.benchmarking.exception.TypeMismatchException;
import de.learnlib.benchmarking.util.SimCountOracle;
import de.learnlib.benchmarking.util.cache.AbstractModelService;
import de.learnlib.cli.option.Type;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import org.jspecify.annotations.Nullable;

public abstract class AbstractLearnerEndpoint<M extends SuffixOutput<String, D>, D, MQOUT, EQOUT> {

    protected final AbstractModelService<M, D> modelService;

    public AbstractLearnerEndpoint(AbstractModelService<M, D> modelService) {
        this.modelService = modelService;
    }

    protected abstract Type getModelType();

    protected abstract MQOUT transformMQOutput(D out);

    protected abstract EQOUT transformEQOutput(DefaultQuery<String, D> out);

    protected abstract InputModelData<String, ? extends M> parseHypothesis(String hypothesis)
            throws IOException, FormatException;

    protected List<MQOUT> answerQueries(@Nullable DBLearnerExperiment experiment, List<List<String>> queries) {

        if (experiment == null) {
            throw new ExperimentNotFoundException();
        }

        if (experiment.getType() != getModelType()) {
            throw new TypeMismatchException();
        }

        final var model = modelService.readFromPath(experiment);
        final var statistics = experiment.getStatistics();

        final int oldQueries = statistics.getNumberOfLearnerQueries();
        final int oldSymbols = statistics.getNumberOfLearnerSymbols();

        final int newQueries = queries.size();
        final List<MQOUT> result = new ArrayList<>(newQueries);
        int newSymbols = 0;

        for (List<String> query : queries) {
            newSymbols += query.size();
            result.add(transformMQOutput(model.computeOutput(query)));
        }

        statistics.setNumberOfLearnerQueries(oldQueries + newQueries);
        statistics.setNumberOfLearnerSymbols(oldSymbols + newSymbols);

        return result;
    }

    protected @Nullable EQOUT findCounterExample(@Nullable DBLearnerExperiment experiment, String hypothesis)
            throws IOException, FormatException {

        if (experiment == null) {
            throw new ExperimentNotFoundException();
        }

        if (experiment.getType() != getModelType()) {
            throw new TypeMismatchException();
        }

        final var model = parseHypothesis(hypothesis);
        final var hyp = model.model;
        final var alphabet = model.alphabet;

        if (!(new HashSet<>(alphabet).equals(new HashSet<>(experiment.getAlphabet())))) {
            throw new IllegalArgumentException("The provided hypothesis has a mismatching alphabet");
        }

        var sul = modelService.readFromPath(experiment);
        var mqo = new SimCountOracle<>(sul);
        var eqo = modelService.getEQO(experiment.getEqoConfig(), mqo);

        var ce = eqo.findCounterExample(hyp, alphabet);

        final var statistics = experiment.getStatistics();
        final int oldQueries = statistics.getNumberOfTesterQueries();
        final int oldSymbols = statistics.getNumberOfTesterSymbols();
        final int newQueries = mqo.getNumberOfQueries();
        final int newSymbols = mqo.getNumberOfSymbols();

        statistics.setNumberOfTesterQueries(oldQueries + newQueries);
        statistics.setNumberOfTesterSymbols(oldSymbols + newSymbols);

        if (ce == null) {
            return null;
        } else {
            return transformEQOutput(ce);
        }
    }
}
