package de.learnlib.benchmarking.util.runner;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.DBRunnerExperiment;
import de.learnlib.benchmarking.entity.DBTesterExperiment;
import de.learnlib.benchmarking.util.cache.ModelServiceDFA;
import de.learnlib.benchmarking.util.cache.ModelServiceMealy;
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

    private final ModelServiceDFA modelServiceDFA;
    private final ModelServiceMealy modelServiceMealy;

    public RunnerManager(ModelServiceDFA modelServiceDFA, ModelServiceMealy modelServiceMealy) {
        this.modelServiceDFA = modelServiceDFA;
        this.modelServiceMealy = modelServiceMealy;
    }

    public boolean testEquivalence(DBExperiment experiment, String hypothesis) throws IOException, FormatException {
        return switch (experiment.getType()) {
            case DFA -> modelServiceDFA.checkEquivalenceWithHypothesis(experiment, hypothesis);
            case MEALY -> modelServiceMealy.checkEquivalenceWithHypothesis(experiment, hypothesis);
            default -> throw new IllegalArgumentException();
        };
    }

    public void startLearningProcessSync(DBTesterExperiment experiment) {
        switch (experiment.getType()) {
            case DFA -> modelServiceDFA.startLearner(experiment);
            case MEALY -> modelServiceMealy.startLearner(experiment);
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
        options.type = type;

        var runnable = switch (type) {
            case DFA -> modelServiceDFA.createExperimentRunner(emf, experiment, eqoConfig, options);
            case MEALY -> modelServiceMealy.createExperimentRunner(emf, experiment, eqoConfig, options);
            default -> throw new IllegalStateException();
        };

        EXECUTOR_SERVICE.submit(runnable);
    }

}
