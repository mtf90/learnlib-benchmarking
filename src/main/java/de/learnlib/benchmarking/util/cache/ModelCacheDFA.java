package de.learnlib.benchmarking.util.cache;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.entity.DBRunnerExperiment;
import de.learnlib.benchmarking.util.runner.RunnerJob;
import de.learnlib.cli.factory.EQOFactory;
import de.learnlib.cli.factory.LearnerFactory;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.util.Constructor;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import io.avaje.jsonb.JsonType;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManagerFactory;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.Automata;

@Singleton
public class ModelCacheDFA extends AbstractModelCache<DFA<?, String>, Boolean> {

    public ModelCacheDFA(JsonType<EQOConfig> eqoSerializer) {
        super(Path.of("dfa"), eqoSerializer);
    }

    @Override
    public EquivalenceOracle<DFA<?, String>, String, Boolean> getEQO(EQOConfig eqoConfig,
                                                                     MembershipOracle<String, Boolean> mqo) {
        final var options = new Options();

        options.sul = Collections.emptyList();
        options.eqos = eqoConfig.oracles();
        options.eqoParams = eqoConfig.params();

        return EQOFactory.getRegularAcceptorOracles(options, mqo, new DummySerializer<>());
    }

    @Override
    public Runnable createExperimentRunner(EntityManagerFactory emf,
                                           DBRunnerExperiment experiment,
                                           EQOConfig eqoConfig,
                                           Options options) {
        return new RunnerJob<>(experiment,
                               readFromPath(experiment),
                               emf,
                               mqo -> getEQO(eqoConfig, mqo),
                               LearnerFactory.getDFALearner(options));
    }

    @Override
    public Constructor<Alphabet<String>, DFA<?, String>, String, Boolean, MembershipOracle<String, Boolean>> getLearnerConstructor(
            Options options) {
        return LearnerFactory.getDFALearner(options);
    }

    @Override
    protected boolean testEquivalence(DFA<?, String> sul, DFA<?, String> hyp, Collection<String> inputs) {
        return Automata.testEquivalence(sul, hyp, inputs);
    }

    @Override
    protected void writeHypothesis(DFA<?, String> hypothesis, Collection<String> alphabet, Writer w)
            throws IOException {
        GraphDOT.write(hypothesis, alphabet, w);
    }

    @Override
    public InputModelData<String, ? extends DFA<?, String>> readFromDot(String dot)
            throws IOException, FormatException {
        return DOTParsers.dfa().readModel(dot.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public InputModelData<String, ? extends DFA<?, String>> readFromPath(String path)
            throws IOException, FormatException {
        return readDFA(path);
    }

    public static InputModelData<String, ? extends DFA<?, String>> readDFA(String path)
            throws IOException, FormatException {
        return DOTParsers.dfa().readModel(ROOT.resolve(path).toFile());
    }
}
