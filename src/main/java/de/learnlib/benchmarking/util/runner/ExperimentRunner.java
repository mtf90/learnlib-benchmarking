package de.learnlib.benchmarking.util.runner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.util.SimCountOracle;
import de.learnlib.cli.util.Constructor;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import jakarta.persistence.EntityManagerFactory;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.concept.SuffixOutput;

public class ExperimentRunner<M extends SuffixOutput<String, D>, D> {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private final long experimentId;
    private final EntityManagerFactory emf;
    private final LearningAlgorithm<M, String, D> learner;
    private final SimCountOracle<M, String, D> oracle;
    private Future<?> currentJob;

    public ExperimentRunner(DBExperiment experiment,
                            EntityManagerFactory emf,
                            M model,
                            Constructor<Alphabet<String>, M, String, D, MembershipOracle<String, D>> constructor) {
        this.experimentId = experiment.getId();
        this.emf = emf;
        this.oracle = new SimCountOracle<>(model);
        this.learner = constructor.constructLearner(Alphabets.fromCollection(experiment.getAlphabet()), this.oracle);
    }

    public void start() {
        this.currentJob = EXECUTOR_SERVICE.submit(new StartJob());
    }

    public M getHypothesis() throws ExecutionException, InterruptedException {
        // wait for potential previous job to finish
        currentJob.get();
        return learner.getHypothesisModel();
    }

    public void refine(DefaultQuery<String, D> ce) throws ExecutionException, InterruptedException {
        // wait for potential previous job to finish
        currentJob.get();
        currentJob = EXECUTOR_SERVICE.submit(new RefineJob(ce));
    }

    private abstract class AbstractJob implements Runnable {

        protected void updateStatistics() {
            var em = emf.createEntityManager();
            var tx = em.getTransaction();
            tx.begin();

            var experiment = em.find(DBExperiment.class, experimentId);
            var statistics = experiment.getStatistics();
            statistics.setNumberOfLearnerQueries(oracle.getNumberOfQueries());
            statistics.setNumberOfLearnerSymbols(oracle.getNumberOfSymbols());

            try {
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            } finally {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }
    }

    private class StartJob extends AbstractJob implements Runnable {

        @Override
        public void run() {
            learner.startLearning();
            updateStatistics();
        }
    }

    private class RefineJob extends AbstractJob implements Runnable {

        private final DefaultQuery<String, D> ce;

        private RefineJob(DefaultQuery<String, D> ce) {
            this.ce = ce;
        }

        @Override
        public void run() {
            learner.refineHypothesis(ce);
            updateStatistics();
        }
    }
}
