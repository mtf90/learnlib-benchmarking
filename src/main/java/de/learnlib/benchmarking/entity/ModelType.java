package de.learnlib.benchmarking.entity;

import java.io.IOException;

import de.learnlib.benchmarking.util.cache.ModelCacheDFA;
import de.learnlib.benchmarking.util.cache.ModelCacheMealy;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;

public enum ModelType {
    DFA {
        @Override
        public InputModelData<String, ?> loadModel(String path) throws IOException, FormatException {
            return ModelCacheDFA.readDFA(path);
        }
    },
    MEALY {
        @Override
        public InputModelData<String, ?> loadModel(String path) throws IOException, FormatException {
            return ModelCacheMealy.readMealy(path);
        }
    },
    MOORE {
        @Override
        public InputModelData<String, ?> loadModel(String path) {
            throw new UnsupportedOperationException();
        }
    };

    public abstract InputModelData<String, ?> loadModel(String path) throws IOException, FormatException;
}
