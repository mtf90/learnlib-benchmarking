package de.learnlib.benchmarking.util.cache;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.cli.option.Type;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import io.avaje.jsonb.JsonType;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.Output;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractModelCache<M extends Output<I, D>, I, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModelCache.class);
    protected static final Path ROOT = Path.of(System.getProperty("de.learnlib.systems.root", "systems/"));

    private final Map<String, InputModelData<I, M>> cache;
    private final JsonType<EQOConfig> eqoSerializer;

    public static InputModelData<String, ?> loadModel(Type type, String path) throws IOException, FormatException {
        return switch (type) {
            case DFA -> ModelCacheDFA.readDFA(path);
            case MEALY -> ModelCacheMealy.readMealy(path);
            default -> throw new UnsupportedOperationException();
        };
    }

    public AbstractModelCache(Path prefix, JsonType<EQOConfig> eqoSerializer) {
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
        InputModelData<I, ? extends M> entry = cache.get(model);

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

    public EquivalenceOracle<M, I, D> getEQO(String eqoConfig, MembershipOracle<I, D> mqo) {
        return getEQO(eqoSerializer.fromJson(eqoConfig), mqo);
    }

    public abstract EquivalenceOracle<M, I, D> getEQO(EQOConfig eqoConfig, MembershipOracle<I, D> mqo);

    public abstract InputModelData<I, ? extends M> readFromDot(String dot) throws IOException, FormatException;

    public abstract InputModelData<I, ? extends M> readFromPath(String path) throws IOException, FormatException;

    protected static class DummySerializer<I, M> implements InputModelSerializer<I, M> {

        @Override
        public void writeModel(OutputStream outputStream, M m, Alphabet<I> alphabet){}
    }
}
