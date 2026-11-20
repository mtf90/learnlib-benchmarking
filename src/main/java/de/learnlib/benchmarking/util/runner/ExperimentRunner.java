package de.learnlib.benchmarking.util.runner;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.util.SimCountOracle;
import de.learnlib.cli.util.Constructor;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.concept.SuffixOutput;

public class ExperimentRunner<M extends SuffixOutput<String, D>, D> {

    private final LearningAlgorithm<M, String, D> learner;
    private final SimCountOracle<M, String, D> oracle;

    public ExperimentRunner(DBExperiment experiment,
                            M model,
                            Constructor<Alphabet<String>, M, String, D, MembershipOracle<String, D>> constructor) {
        this.oracle = new SimCountOracle<>(model);
        this.learner = constructor.constructLearner(Alphabets.fromCollection(experiment.getAlphabet()), this.oracle);
    }

    public void start(DBExperiment experiment) {
        this.learner.startLearning();
        updateStatistics(experiment);
    }

    public M getHypothesis() {
        return learner.getHypothesisModel();
    }

    public void refine(DBExperiment experiment, DefaultQuery<String, D> ce) {
        learner.refineHypothesis(ce);
        updateStatistics(experiment);
    }

    private void updateStatistics(DBExperiment experiment) {
        var statistics = experiment.getStatistics();
        statistics.setNumberOfLearnerQueries(oracle.getNumberOfQueries());
        statistics.setNumberOfLearnerSymbols(oracle.getNumberOfSymbols());
    }
}
