package de.learnlib.benchmarking.util.runner;

import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.DBRunnerExperiment;
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

    public ExperimentRunner<?, ?> createAndRegisterRunner(DBExperiment experiment, Learner learner, Type type) {
        final var options = new Options();
        options.learner = learner;

        return switch (type) {
            case DFA -> createAndRegisterRunnerDFA(experiment, options);
            case MEALY -> createAndRegisterRunnerMealy(experiment, options);
            default -> throw new IllegalStateException();
        };
    }

    private ExperimentRunner<?, ?> createAndRegisterRunnerDFA(DBExperiment experiment, Options options) {
        final var runner = new ExperimentRunner<>(experiment,
                                                  modelCacheDFA.readFromPath(experiment),
                                                  LearnerFactory.getDFALearner(options));

        runnerCacheDFA.putRunner(experiment.getId(), runner);
        return runner;
    }

    private ExperimentRunner<?, ?> createAndRegisterRunnerMealy(DBExperiment experiment, Options options) {
        final var runner = new ExperimentRunner<>(experiment,
                                                  modelCacheMealy.readFromPath(experiment),
                                                  LearnerFactory.getMealyLearner(options));

        runnerCacheMealy.putRunner(experiment.getId(), runner);
        return runner;
    }

    public void submitRunner(DBRunnerExperiment experiment,
                             EntityManagerFactory emf,
                             Learner learner,
                             Type type,
                             EQOConfig eqoConfig) {
        final var options = new Options();
        options.learner = learner;

        switch (type) {
            case DFA -> submitRunnerDFA(emf, experiment, eqoConfig, options);
            case MEALY -> submitRunnerMealy(emf, experiment, eqoConfig, options);
            default -> throw new IllegalStateException();
        }
    }

    private void submitRunnerDFA(EntityManagerFactory emf,
                                 DBRunnerExperiment experiment,
                                 EQOConfig eqoConfig,
                                 Options options) {
        ExperimentRunner2.submitRunner(experiment,
                                       modelCacheDFA.readFromPath(experiment),
                                       emf,
                                       mqo -> modelCacheDFA.getEQO(eqoConfig, mqo),
                                       LearnerFactory.getDFALearner(options));
    }

    private void submitRunnerMealy(EntityManagerFactory emf,
                                   DBRunnerExperiment experiment,
                                   EQOConfig eqoConfig,
                                   Options options) {
        ExperimentRunner2.submitRunner(experiment,
                                       modelCacheMealy.readFromPath(experiment),
                                       emf,
                                       mqo -> modelCacheMealy.getEQO(eqoConfig, mqo),
                                       LearnerFactory.getMealyLearner(options));
    }

}
