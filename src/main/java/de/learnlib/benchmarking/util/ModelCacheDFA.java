package de.learnlib.benchmarking.util;

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
import jakarta.inject.Singleton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.dot.DOTInputModelData;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ModelCacheDFA {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelCacheDFA.class);
    private static final Path ROOT = Path.of(System.getProperty("de.learnlib.systems.root", "systems/"));
    private static final Path PREFIX = Path.of("dfa");

    private final Map<String, InputModelData<Integer, DFA<?, Integer>>> cache;

    public ModelCacheDFA() {
        cache = new ConcurrentHashMap<>(new LRUCache<>(64));

        Path startPath = ROOT.resolve(PREFIX);

        try (Stream<Path> stream = Files.walk(startPath)) {
            stream.filter(Files::isRegularFile).map(ROOT::relativize).forEach(n -> LOGGER.info("Found model '{}'", n));

        } catch (IOException e) {
            LOGGER.error("Error while walking the directory", e);
        }
    }

    public EquivalenceOracle<DFA<?, Integer>, Integer, Boolean> getEQO(MembershipOracle<Integer, Boolean> mqo) {
        return new RandomWpMethodEQOracle<>(mqo, 0, 0);
    }

    public DFA<?, Integer> readDFA(DBExperiment experiment) {

        var model = experiment.getModel();
        InputModelData<Integer, ? extends DFA<?, Integer>> entry = cache.get(model);

        if (entry == null) {
            try {
                entry = ModelCacheDFA.readDFA(model);
            } catch (IOException | FormatException e) {
                throw new RuntimeException("Could not load model", e);
            }
        }

        return entry.model;
    }

    public static DOTInputModelData<Integer, Integer, CompactDFA<Integer>> readDFA(String path)
            throws IOException, FormatException {
        return DOTParsers.dfa(DOTParsers.DEFAULT_FSA_NODE_PARSER, ModelCacheDFA::parseInputs)
                         .readModel(ROOT.resolve(path).toFile());
    }

    public static int parseInputs(Map<String, String> keys) {
        return Integer.parseInt(keys.get(EdgeAttrs.LABEL));
    }
}
