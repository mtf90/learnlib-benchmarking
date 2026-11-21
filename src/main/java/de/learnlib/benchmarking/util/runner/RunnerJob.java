package de.learnlib.benchmarking.util.runner;

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

public class RunnerJob<M extends SuffixOutput<String, D> & FiniteRepresentation, D> implements Runnable {

    private final long experimentId;
    private final EntityManagerFactory emf;
    private final Experiment<M, String, D> exp;
    private final SimCountOracle<M, String, D> learnerCounter;
    private final SimCountOracle<M, String, D> testerCounter;

    public RunnerJob(DBRunnerExperiment experiment,
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

        this.experimentId = experiment.getId();
        this.emf = emf;
        this.exp = exp;
        this.learnerCounter = learnerCounter;
        this.testerCounter = testerCounter;
    }

    @Override
    public void run() {
        RunnerManager.LOGGER.info("Starting job for experiment {}", experimentId);
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
            RunnerManager.LOGGER.warn("Error while running experiment", e);
            throw e;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }

        RunnerManager.LOGGER.info("Finished job for experiment {}", experimentId);
    }
}
