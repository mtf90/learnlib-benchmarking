package de.learnlib.benchmarking.util.runner;

import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.util.cache.ModelCacheDFA;
import de.learnlib.benchmarking.util.cache.ModelCacheMealy;
import de.learnlib.cli.factory.LearnerFactory;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.option.Type;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManagerFactory;

@Singleton
public class RunnerManager {

    private final ModelCacheDFA modelCacheDFA;
    private final ModelCacheMealy modelCacheMealy;

    private final RunnerCacheDFA runnerCacheDFA;
    private final RunnerCacheMealy runnerCacheMealy;

    public RunnerManager(ModelCacheDFA modelCacheDFA,
                         ModelCacheMealy modelCacheMealy,
                         RunnerCacheDFA runnerCacheDFA,
                         RunnerCacheMealy runnerCacheMealy) {
        this.modelCacheDFA = modelCacheDFA;
        this.modelCacheMealy = modelCacheMealy;
        this.runnerCacheDFA = runnerCacheDFA;
        this.runnerCacheMealy = runnerCacheMealy;
    }

    public ExperimentRunner<?, ?> createAndRegisterRunner(DBExperiment experiment,
                                                          EntityManagerFactory emf,
                                                          Learner learner,
                                                          Type type) {
        final var options = new Options();
        options.learner = learner;

        return switch (type) {
            case DFA -> createAndRegisterRunnerDFA(experiment, emf, options);
            case MEALY -> createAndRegisterRunnerMealy(experiment, emf, options);
            default -> throw new IllegalStateException();
        };
    }

    private ExperimentRunner<?, ?> createAndRegisterRunnerDFA(DBExperiment experiment,
                                                              EntityManagerFactory emf,
                                                              Options options) {
        final var runner = new ExperimentRunner<>(experiment,
                                                  emf,
                                                  modelCacheDFA.readFromPath(experiment),
                                                  LearnerFactory.getDFALearner(options));

        runnerCacheDFA.putRunner(experiment.getId(), runner);
        return runner;
    }

    private ExperimentRunner<?, ?> createAndRegisterRunnerMealy(DBExperiment experiment,
                                                                EntityManagerFactory emf,
                                                                Options options) {
        final var runner = new ExperimentRunner<>(experiment,
                                                  emf,
                                                  modelCacheMealy.readFromPath(experiment),
                                                  LearnerFactory.getMealyLearner(options));

        runnerCacheMealy.putRunner(experiment.getId(), runner);
        return runner;
    }

}
