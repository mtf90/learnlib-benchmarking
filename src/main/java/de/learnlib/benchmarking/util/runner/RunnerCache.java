package de.learnlib.benchmarking.util.runner;

import java.util.concurrent.ConcurrentHashMap;

import net.automatalib.automaton.concept.SuffixOutput;

public abstract class RunnerCache<M extends SuffixOutput<String, D>, D> {

    private static final ConcurrentHashMap<Long, ExperimentRunner<?, ?>> POOL = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked") // cast is fine because we only access the map through our type-safe methods
    public ExperimentRunner<M, D> getRunner(long id) {
        return (ExperimentRunner<M, D>) POOL.get(id);
    }

    public void putRunner(long id, ExperimentRunner<M, D> runner) {
        POOL.put(id, runner);
    }

    public void removeRunner(long id) {
        POOL.remove(id);
    }

}
