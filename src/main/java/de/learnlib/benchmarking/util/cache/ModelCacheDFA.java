package de.learnlib.benchmarking.util.cache;

import java.io.IOException;
import java.nio.file.Path;

import jakarta.inject.Singleton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.dot.DOTParsers;

@Singleton
public class ModelCacheDFA extends AbstractModelCache<DFA<?, String>, String, Boolean> {

    public ModelCacheDFA() {
        super(Path.of("dfa"));
    }

    @Override
    public InputModelData<String, ? extends DFA<?, String>> readModel(String path)
            throws IOException, FormatException {
        return readDFA(path);
    }

    public static InputModelData<String, ? extends DFA<?, String>> readDFA(String path)
            throws IOException, FormatException {
        return DOTParsers.dfa().readModel(ROOT.resolve(path).toFile());
    }
}
