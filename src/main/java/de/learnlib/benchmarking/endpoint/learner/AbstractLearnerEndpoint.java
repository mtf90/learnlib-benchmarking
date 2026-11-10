package de.learnlib.benchmarking.endpoint.learner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.learnlib.benchmarking.dto.QueryDTO;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.ModelType;
import de.learnlib.benchmarking.util.SimCountOracle;
import de.learnlib.benchmarking.util.cache.AbstractModelCache;
import net.automatalib.automaton.UniversalDeterministicAutomaton.RegularAutomaton;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import org.jspecify.annotations.Nullable;

public abstract class AbstractLearnerEndpoint<M extends RegularAutomaton<?, String, ?, ?, ?> & SuffixOutput<String, D>, D, OUT> {

    private final AbstractModelCache<M, String, D> modelCache;

    public AbstractLearnerEndpoint(AbstractModelCache<M, String, D> modelCache) {
        this.modelCache = modelCache;
    }

    protected abstract ModelType getModelType();

    protected abstract OUT transformOutput(D out);

    protected abstract InputModelData<String, ? extends M> parseHypothesis(String hypothesis)
            throws IOException, FormatException;

    protected List<OUT> answerQueries(DBExperiment experiment, List<QueryDTO> queries) {

        if (experiment.getType() != getModelType()) {
            throw new IllegalArgumentException("Type mismatch between endpoint and experiment");
        }

        final var model = modelCache.readModel(experiment);
        final var statistics = experiment.getStatistics();

        final int oldQueries = statistics.getNumberOfLearnerQueries();
        final int oldSymbols = statistics.getNumberOfLearnerSymbols();

        final int newQueries = queries.size();
        final List<OUT> result = new ArrayList<>(newQueries);
        int newSymbols = 0;

        for (QueryDTO query : queries) {
            newSymbols += query.symbols().size();
            result.add(transformOutput(model.computeOutput(query.symbols())));
        }

        statistics.setNumberOfLearnerQueries(oldQueries + newQueries);
        statistics.setNumberOfLearnerSymbols(oldSymbols + newSymbols);

        return result;
    }

    protected @Nullable QueryDTO findCounterExample(DBExperiment experiment, String hypothesis)
            throws IOException, FormatException {

        if (experiment.getType() != getModelType()) {
            throw new IllegalArgumentException();
        }

        final var model = parseHypothesis(hypothesis);
        final var hyp = model.model;
        final var alphabet = model.alphabet;

        if (!(new HashSet<>(alphabet).equals(new HashSet<>(experiment.getAlphabet())))) {
            throw new IllegalArgumentException("The provided hypothesis has a mismatching alphabet");
        }

        var mqo = new SimCountOracle<>(hyp);
        var eqo = modelCache.getEQO(mqo);

        var ce = eqo.findCounterExample(hyp, alphabet);

        final var statistics = experiment.getStatistics();
        final int oldQueries = statistics.getNumberOfTesterQueries();
        final int oldSymbols = statistics.getNumberOfTesterSymbols();
        final int newQueries = mqo.getNumberOfQueries();
        final int newSymbols = mqo.getNumberOfSymbols();

        statistics.setNumberOfTesterQueries(oldQueries + newQueries);
        statistics.setNumberOfLearnerSymbols(oldSymbols + newSymbols);

        if (ce == null) {
            return null;
        } else {
            return new QueryDTO(ce.getInput().asList());
        }
    }
}
