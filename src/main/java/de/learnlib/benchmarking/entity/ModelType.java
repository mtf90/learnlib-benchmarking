package de.learnlib.benchmarking.entity;

import java.io.IOException;

import de.learnlib.benchmarking.util.ModelCacheDFA;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;

public enum ModelType {
    DFA {
        @Override
        public InputModelData<?, ?> loadModel(String path) throws IOException, FormatException {
            return ModelCacheDFA.readDFA(path);
        }
    },
    MEALY {
        @Override
        public InputModelData<?, ?> loadModel(String path) {
            throw new UnsupportedOperationException();
        }
    },
    MOORE {
        @Override
        public InputModelData<?, ?> loadModel(String path) {
            throw new UnsupportedOperationException();
        }
    };

    public abstract InputModelData<?, ?> loadModel(String path) throws IOException, FormatException;
}
