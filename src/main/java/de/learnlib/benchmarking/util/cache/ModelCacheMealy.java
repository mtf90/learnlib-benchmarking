package de.learnlib.benchmarking.util.cache;

import java.io.IOException;
import java.nio.file.Path;

import jakarta.inject.Singleton;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.word.Word;

@Singleton
public class ModelCacheMealy extends AbstractModelCache<MealyMachine<?, String, ?, String>, String, Word<String>> {

    public ModelCacheMealy() {
        super(Path.of("mealy"));
    }

    @Override
    public InputModelData<String, ? extends MealyMachine<?, String, ?, String>> readModel(String path)
            throws IOException, FormatException {
        return readMealy(path);
    }

    public static InputModelData<String, ? extends MealyMachine<?, String, ?, String>> readMealy(String path)
            throws IOException, FormatException {
        return DOTParsers.mealy().readModel(ROOT.resolve(path).toFile());
    }
}
