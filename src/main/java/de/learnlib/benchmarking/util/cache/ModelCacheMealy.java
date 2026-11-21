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
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;

@Singleton
public class ModelCacheMealy extends AbstractModelCache<MealyMachine<?, String, ?, String>, Word<String>> {

    public ModelCacheMealy(JsonType<EQOConfig> eqoSerializer) {
        super(Path.of("mealy"), eqoSerializer);
    }

    @Override
    public EquivalenceOracle<MealyMachine<?, String, ?, String>, String, Word<String>> getEQO(EQOConfig eqoConfig,
                                                                                              MembershipOracle<String, Word<String>> mqo) {
        final var options = new Options();

        options.sul = Collections.emptyList();
        options.eqos = eqoConfig.oracles();
        options.eqoParams = eqoConfig.params();

        return EQOFactory.getRegularOutputOracles(options, mqo, new DummySerializer<>());
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
                               LearnerFactory.getMealyLearner(options));
    }

    @Override
    public Constructor<Alphabet<String>, MealyMachine<?, String, ?, String>, String, Word<String>, MembershipOracle<String, Word<String>>> getLearnerConstructor(
            Options options) {
        return LearnerFactory.getMealyLearner(options);
    }

    @Override
    protected boolean testEquivalence(MealyMachine<?, String, ?, String> sul,
                                      MealyMachine<?, String, ?, String> hyp,
                                      Collection<String> inputs) {
        return Automata.testEquivalence(sul, hyp, inputs);
    }

    @Override
    protected void writeHypothesis(MealyMachine<?, String, ?, String> hypothesis, Collection<String> alphabet, Writer w)
            throws IOException {
        GraphDOT.write(hypothesis, alphabet, w);
    }

    @Override
    public InputModelData<String, ? extends MealyMachine<?, String, ?, String>> readFromDot(String dot)
            throws IOException, FormatException {
        return DOTParsers.mealy().readModel(dot.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public InputModelData<String, ? extends MealyMachine<?, String, ?, String>> readFromPath(String path)
            throws IOException, FormatException {
        return readMealy(path);
    }

    public static InputModelData<String, ? extends MealyMachine<?, String, ?, String>> readMealy(String path)
            throws IOException, FormatException {
        return DOTParsers.mealy().readModel(ROOT.resolve(path).toFile());
    }
}
