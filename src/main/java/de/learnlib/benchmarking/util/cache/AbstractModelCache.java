package de.learnlib.benchmarking.util.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.equivalence.RandomWpMethodEQOracle;
import net.automatalib.automaton.UniversalDeterministicAutomaton.RegularAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractModelCache<M extends RegularAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModelCache.class);
    protected static final Path ROOT = Path.of(System.getProperty("de.learnlib.systems.root", "systems/"));

    private final Map<String, InputModelData<I, M>> cache;

    public AbstractModelCache(Path prefix) {
        cache = new ConcurrentHashMap<>(new LRUCache<>(64));

        Path startPath = ROOT.resolve(prefix);

        try (Stream<Path> stream = Files.walk(startPath)) {
            stream.filter(Files::isRegularFile).map(ROOT::relativize).forEach(n -> LOGGER.info("Found model '{}'", n));
        } catch (IOException e) {
            LOGGER.error("Error while walking the directory", e);
        }
    }

    public EquivalenceOracle<M, I, D> getEQO(MembershipOracle<I, D> mqo) {
        return new RandomWpMethodEQOracle<>(mqo, 0, 0);
    }

    public M readModel(DBExperiment experiment) {

        var model = experiment.getModel();
        InputModelData<I, ? extends M> entry = cache.get(model);

        if (entry == null) {
            try {
                entry = readModel(model);
                cache.put(model, new InputModelData<>(entry.model, entry.alphabet));
            } catch (IOException | FormatException e) {
                throw new RuntimeException("Could not load model", e);
            }
        }

        return entry.model;
    }

    public abstract InputModelData<I, ? extends M> readModel(String path) throws IOException, FormatException;

}
