package de.learnlib.benchmarking.util.cache;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import de.learnlib.Resumable;
import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.DBRunnerExperiment;
import de.learnlib.benchmarking.entity.DBTesterExperiment;
import de.learnlib.benchmarking.util.ForyService;
import de.learnlib.benchmarking.util.SimCountOracle;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.option.Type;
import de.learnlib.cli.util.Constructor;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import io.avaje.jsonb.JsonType;
import jakarta.persistence.EntityManagerFactory;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractModelService<M extends SuffixOutput<String, D>, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModelService.class);
    protected static final Path ROOT = Path.of(System.getProperty("de.learnlib.systems.root", "systems/"));

    private final Map<String, InputModelData<String, M>> cache;
    private final JsonType<EQOConfig> eqoSerializer;

    public static InputModelData<String, ?> loadModel(Type type, String path) throws IOException, FormatException {
        return switch (type) {
            case DFA -> ModelServiceDFA.readDFA(path);
            case MEALY -> ModelServiceMealy.readMealy(path);
            default -> throw new UnsupportedOperationException();
        };
    }

    public AbstractModelService(Path prefix, JsonType<EQOConfig> eqoSerializer) {
        this.cache = new ConcurrentHashMap<>(new LRUCache<>(64));
        this.eqoSerializer = eqoSerializer;

        Path startPath = ROOT.resolve(prefix);

        try (Stream<Path> stream = Files.walk(startPath)) {
            stream.filter(Files::isRegularFile).map(ROOT::relativize).forEach(n -> LOGGER.info("Found model '{}'", n));
        } catch (IOException e) {
            LOGGER.error("Error while walking the directory", e);
        }
    }

    public M readFromPath(DBExperiment experiment) {

        var model = experiment.getModel();
        InputModelData<String, ? extends M> entry = cache.get(model);

        if (entry == null) {
            try {
                entry = readFromPath(model);
                cache.put(model, new InputModelData<>(entry.model, entry.alphabet));
            } catch (IOException | FormatException e) {
                throw new RuntimeException("Could not load model", e);
            }
        }

        return entry.model;
    }

    public EquivalenceOracle<M, String, D> getEQO(String eqoConfig, MembershipOracle<String, D> mqo) {
        return getEQO(eqoSerializer.fromJson(eqoConfig), mqo);
    }

    private String hypToString(M hyp, Collection<String> alphabet) throws IOException {
        try (Writer w = new StringWriter()) {
            writeHypothesis(hyp, alphabet, w);
            return w.toString();
        }
    }

    public abstract EquivalenceOracle<M, String, D> getEQO(EQOConfig eqoConfig, MembershipOracle<String, D> mqo);

    public abstract Runnable createExperimentRunner(EntityManagerFactory emf,
                                                    DBRunnerExperiment experiment,
                                                    EQOConfig eqoConfig,
                                                    Options options);

    public abstract Constructor<Alphabet<String>, M, String, D, MembershipOracle<String, D>> getLearnerConstructor(
            Options options);

    public void startLearner(DBTesterExperiment experiment) {
        var options = new Options();
        options.learner = experiment.getLearner();
        var alphabet = Alphabets.fromCollection(experiment.getAlphabet());

        var constructor = getLearnerConstructor(options);
        var mqo = new SimCountOracle<>(readFromPath(experiment));
        var learner = constructor.constructLearner(alphabet, mqo);

        if (learner instanceof Resumable<?> resumable) {
            learner.startLearning();
            var statistics = experiment.getStatistics();
            statistics.setNumberOfLearnerQueries(mqo.getNumberOfQueries());
            statistics.setNumberOfLearnerSymbols(mqo.getNumberOfSymbols());
            experiment.setLearnerState(ForyService.toBytes(resumable.suspend()));
        } else {
            throw new IllegalArgumentException("Not a resumable learner");
        }
    }

    public M getHypothesis(DBTesterExperiment experiment) throws IOException {
        var options = new Options();
        options.learner = experiment.getLearner();
        var alphabet = Alphabets.fromCollection(experiment.getAlphabet());

        var constructor = getLearnerConstructor(options);
        var mqo = new SimCountOracle<>(readFromPath(experiment));
        var learner = constructor.constructLearner(alphabet, mqo);

        if (learner instanceof Resumable<?> resumable) {
            resumable.resume(ForyService.fromBytes(experiment.getLearnerState()));
        } else {
            throw new IllegalArgumentException("Not a resumable learner");
        }

        return learner.getHypothesisModel();
    }

    public void refineHypothesis(DBTesterExperiment experiment, DefaultQuery<String, D> counterexample) {
        var options = new Options();
        options.learner = experiment.getLearner();
        var alphabet = Alphabets.fromCollection(experiment.getAlphabet());

        var constructor = getLearnerConstructor(options);
        var mqo = new SimCountOracle<>(readFromPath(experiment));
        var learner = constructor.constructLearner(alphabet, mqo);

        if (learner instanceof Resumable<?> resumable) {
            final var statistics = experiment.getStatistics();
            final var oldNumberOfQueries = statistics.getNumberOfLearnerQueries();
            final var oldNumberOfSymbols = statistics.getNumberOfLearnerSymbols();

            resumable.resume(ForyService.fromBytes(experiment.getLearnerState()));

            learner.refineHypothesis(counterexample);

            statistics.setNumberOfLearnerQueries(oldNumberOfQueries + mqo.getNumberOfQueries());
            statistics.setNumberOfLearnerSymbols(oldNumberOfSymbols + mqo.getNumberOfSymbols());

            experiment.setLearnerState(ForyService.toBytes(resumable.suspend()));
        } else {
            throw new IllegalArgumentException("Not a resumable learner");
        }
    }

    public String getHypothesisAsString(DBTesterExperiment experiment) throws IOException {
        return hypToString(getHypothesis(experiment), experiment.getAlphabet());
    }

    public boolean checkEquivalenceWithHypothesis(DBExperiment experiment, String hypothesis)
            throws IOException, FormatException {
        final var sul = readFromPath(experiment);
        final var data = readFromDot(hypothesis);

        return testEquivalence(sul, data.model, data.alphabet);
    }

    protected abstract boolean testEquivalence(M sul, M hyp, Collection<String> inputs);

    protected abstract void writeHypothesis(M hypothesis, Collection<String> alphabet, Writer w) throws IOException;

    public abstract InputModelData<String, ? extends M> readFromDot(String dot) throws IOException, FormatException;

    public abstract InputModelData<String, ? extends M> readFromPath(String path) throws IOException, FormatException;

    protected static class DummySerializer<I, M> implements InputModelSerializer<I, M> {

        @Override
        public void writeModel(OutputStream outputStream, M m, Alphabet<I> alphabet) {}
    }
}
