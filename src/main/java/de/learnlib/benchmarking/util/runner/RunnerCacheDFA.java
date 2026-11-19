package de.learnlib.benchmarking.util.runner;

import jakarta.inject.Singleton;
import net.automatalib.automaton.fsa.DFA;

@Singleton
public class RunnerCacheDFA extends RunnerCache<DFA<?, String>, Boolean> {}
