package de.learnlib.benchmarking.util.runner;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.DBRunnerExperiment;
import de.learnlib.benchmarking.entity.DBTesterExperiment;
import de.learnlib.benchmarking.util.cache.ModelCacheDFA;
import de.learnlib.benchmarking.util.cache.ModelCacheMealy;
import de.learnlib.cli.factory.LearnerFactory;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.option.Type;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManagerFactory;
import net.automatalib.exception.FormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RunnerManager {

    final static Logger LOGGER = LoggerFactory.getLogger(RunnerJob.class);
    private static final ExecutorService EXECUTOR_SERVICE =
            Executors.newFixedThreadPool(Math.ceilDiv(Runtime.getRuntime().availableProcessors(), 2));

    private final ModelCacheDFA modelCacheDFA;
    private final ModelCacheMealy modelCacheMealy;

    public RunnerManager(ModelCacheDFA modelCacheDFA, ModelCacheMealy modelCacheMealy) {
        this.modelCacheDFA = modelCacheDFA;
        this.modelCacheMealy = modelCacheMealy;
    }

    public boolean testEquivalence(DBExperiment experiment, String hypothesis) throws IOException, FormatException {
        return switch (experiment.getType()) {
            case DFA -> modelCacheDFA.checkEquivalenceWithHypothesis(experiment, hypothesis);
            case MEALY -> modelCacheMealy.checkEquivalenceWithHypothesis(experiment, hypothesis);
            default -> throw new IllegalArgumentException();
        };
    }

    public void startLearningProcessSync(DBTesterExperiment experiment) {
        switch (experiment.getType()) {
            case DFA -> modelCacheDFA.startLearner(experiment);
            case MEALY -> modelCacheMealy.startLearner(experiment);
            default -> throw new IllegalStateException();
        }
    }

    public void submitRunnerAsync(DBRunnerExperiment experiment,
                                  EntityManagerFactory emf,
                                  Learner learner,
                                  Type type,
                                  EQOConfig eqoConfig) {
        final var options = new Options();
        options.learner = learner;

        var runnable = switch (type) {
            case DFA -> modelCacheDFA.createExperimentRunner(emf, experiment, eqoConfig, options);
            case MEALY -> modelCacheMealy.createExperimentRunner(emf, experiment, eqoConfig, options);
            default -> throw new IllegalStateException();
        };

        EXECUTOR_SERVICE.submit(runnable);
    }

}
