package de.learnlib.benchmarking.util.runner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import de.learnlib.benchmarking.entity.DBRunnerExperiment;
import de.learnlib.benchmarking.entity.DBRunnerExperiment.Status;
import de.learnlib.benchmarking.util.SimCountOracle;
import de.learnlib.cli.util.Constructor;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.util.Experiment;
import jakarta.persistence.EntityManagerFactory;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.automaton.concept.SuffixOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimentRunner2 {

    private static Logger LOGGER = LoggerFactory.getLogger(ExperimentRunner2.class);

    private static final ExecutorService EXECUTOR_SERVICE =
            Executors.newFixedThreadPool(Math.ceilDiv(Runtime.getRuntime().availableProcessors(), 2));

    public static <M extends SuffixOutput<String, D> & FiniteRepresentation, D> void submitRunner(DBRunnerExperiment experiment,
                                                                                                  M model,
                                                                                                  EntityManagerFactory emf,
                                                                                                  Function<MembershipOracle<String, D>, EquivalenceOracle<M, String, D>> eqoCreator,
                                                                                                  Constructor<Alphabet<String>, M, String, D, MembershipOracle<String, D>> learnerCreator) {

        var alphabet = Alphabets.fromCollection(experiment.getAlphabet());
        var learnerCounter = new SimCountOracle<>(model);
        var testerCounter = new SimCountOracle<>(model);

        var learner = learnerCreator.constructLearner(alphabet, learnerCounter);
        var eqo = eqoCreator.apply(testerCounter);
        var exp = new Experiment<>(learner, eqo, alphabet);

        EXECUTOR_SERVICE.submit(new Job(experiment.getId(), emf, exp, learnerCounter, testerCounter));
    }

    private static class Job implements Runnable {

        private final long experimentId;
        private final EntityManagerFactory emf;
        private final Experiment<?, ?, ?> exp;
        private final SimCountOracle<?, ?, ?> learnerCounter;
        private final SimCountOracle<?, ?, ?> testerCounter;

        private Job(long experimentId,
                    EntityManagerFactory emf,
                    Experiment<?, ?, ?> exp,
                    SimCountOracle<?, ?, ?> learnerCounter,
                    SimCountOracle<?, ?, ?> testerCounter) {
            this.experimentId = experimentId;
            this.emf = emf;
            this.exp = exp;
            this.learnerCounter = learnerCounter;
            this.testerCounter = testerCounter;
        }

        @Override
        public void run() {
            LOGGER.info("Starting job for experiment {}", experimentId);
            exp.run();

            var em = emf.createEntityManager();
            var tx = em.getTransaction();

            try {
                tx.begin();

                var experiment = em.find(DBRunnerExperiment.class, experimentId);
                experiment.setStatus(Status.FINISHED);

                var statistics = experiment.getStatistics();
                statistics.setNumberOfLearnerQueries(learnerCounter.getNumberOfQueries());
                statistics.setNumberOfLearnerSymbols(learnerCounter.getNumberOfSymbols());
                statistics.setNumberOfTesterQueries(testerCounter.getNumberOfQueries());
                statistics.setNumberOfTesterSymbols(testerCounter.getNumberOfSymbols());

                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                LOGGER.warn("Error while running experiment", e);
                throw e;
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }

            LOGGER.info("Finished job for experiment {}", experimentId);
        }
    }
}
