package de.learnlib.benchmarking.util.runner;

import jakarta.inject.Singleton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

@Singleton
public class RunnerCacheMealy extends RunnerCache<MealyMachine<?, String, ?, String>, Word<String>> {}
