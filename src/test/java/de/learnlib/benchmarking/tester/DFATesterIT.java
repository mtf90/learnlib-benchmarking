package de.learnlib.benchmarking.tester;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.learnlib.benchmarking.ServerLifecycleListener;
import de.learnlib.benchmarking.api.TesterEndpoint;
import de.learnlib.benchmarking.api.TesterEndpointDFA;
import de.learnlib.benchmarking.api.TesterEndpointMealy;
import de.learnlib.benchmarking.dto.AcceptorResponseDTO;
import de.learnlib.cli.option.Type;
import org.testng.Assert;

public class DFATesterIT extends AbstractTesterIT<AcceptorResponseDTO> {

    @Override
    protected TesterEndpoint<AcceptorResponseDTO> getLearnerEndpointModel() {
        return ServerLifecycleListener.getClient().create(TesterEndpointDFA.class);
    }

    @Override
    protected TesterEndpoint<?> getLearnerEndpointOther() {
        return ServerLifecycleListener.getClient().create(TesterEndpointMealy.class);
    }

    @Override
    protected Type getType() {
        return Type.DFA;
    }

    @Override
    protected String getModel() {
        return "dfa/AngluinDFA.dot";
    }

    @Override
    protected Set<String> getInputs() {
        return Set.of("0", "1");
    }

    @Override
    protected String getInitialHypothesis() throws IOException {
        try (var is = DFATesterIT.class.getResourceAsStream("/init_dfa.dot")) {
            Assert.assertNotNull(is);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Override
    protected String getFinalHypothesis() throws IOException {
        try (var is = DFATesterIT.class.getResourceAsStream("/final_dfa.dot")) {
            Assert.assertNotNull(is);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Override
    protected List<List<String>> getNonCounterexamples() {
        return Arrays.asList(List.of("0", "1", "1", "1"), List.of("0", "1", "1", "1", "1"));
    }

    @Override
    protected List<String> getCounterexample() {
        return Arrays.asList("0", "1", "0", "1");
    }

    @Override
    protected boolean testCounterexampleInput(AcceptorResponseDTO counterexample) {
        return counterexample.input().equals(getCounterexample());
    }

    @Override
    protected boolean testCounterexampleOutput(AcceptorResponseDTO counterexample) {
        return counterexample.output();
    }
}
