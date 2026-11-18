package de.learnlib.benchmarking.util.cache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;

import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.cli.factory.EQOFactory;
import de.learnlib.cli.option.Options;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import io.avaje.jsonb.JsonType;
import jakarta.inject.Singleton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.dot.DOTParsers;

@Singleton
public class ModelCacheDFA extends AbstractModelCache<DFA<?, String>, String, Boolean> {

    private final JsonType<EQOConfig> eqoSerializer;

    public ModelCacheDFA(JsonType<EQOConfig> eqoSerializer) {
        super(Path.of("dfa"));
        this.eqoSerializer = eqoSerializer;
    }

    @Override
    public EquivalenceOracle<DFA<?, String>, String, Boolean> getEQO(DBExperiment experiment,
                                                                     MembershipOracle<String, Boolean> mqo) {
        final var eqoConfig = eqoSerializer.fromJson(experiment.getEqoConfig());
        final var options = new Options();

        options.sul = Collections.emptyList();
        options.eqos = eqoConfig.oracles();
        options.eqoParams = eqoConfig.params();

        return EQOFactory.getRegularAcceptorOracles(options, mqo, new DummySerializer<>());
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
