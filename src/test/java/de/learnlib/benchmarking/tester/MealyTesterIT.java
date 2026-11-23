package de.learnlib.benchmarking.tester;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.learnlib.benchmarking.ServerLifecycleListener;
import de.learnlib.benchmarking.api.tester.TesterEndpoint;
import de.learnlib.benchmarking.api.tester.TesterEndpointDFA;
import de.learnlib.benchmarking.api.tester.TesterEndpointMealy;
import de.learnlib.benchmarking.dto.TransducerResponseDTO;
import de.learnlib.cli.option.Type;
import org.testng.Assert;

public class MealyTesterIT extends AbstractTesterIT<TransducerResponseDTO> {

    @Override
    protected TesterEndpoint<TransducerResponseDTO> getLearnerEndpointModel() {
        return ServerLifecycleListener.getClient().create(TesterEndpointMealy.class);
    }

    @Override
    protected TesterEndpoint<?> getLearnerEndpointOther() {
        return ServerLifecycleListener.getClient().create(TesterEndpointDFA.class);
    }

    @Override
    protected Type getType() {
        return Type.MEALY;
    }

    @Override
    protected String getModel() {
        return "mealy/CoffeeMealyMachine.dot";
    }

    @Override
    protected Set<String> getInputs() {
        return Set.of("5ct?", "10ct?", "button?");
    }

    @Override
    protected String getInitialHypothesis() throws IOException {
        try (var is = MealyTesterIT.class.getResourceAsStream("/init_mealy.dot")) {
            Assert.assertNotNull(is);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Override
    protected String getFinalHypothesis() throws IOException {
        try (var is = MealyTesterIT.class.getResourceAsStream("/final_mealy.dot")) {
            Assert.assertNotNull(is);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Override
    protected List<List<String>> getNonCounterexamples() {
        return Arrays.asList(List.of("5ct?", "5ct?"), List.of("button?", "button?", "button?", "button?"));
    }

    @Override
    protected List<String> getCounterexample() {
        return Arrays.asList("5ct?", "10ct?", "button?");
    }

    @Override
    protected boolean testCounterexampleInput(TransducerResponseDTO counterexample) {
        return counterexample.input().equals(getCounterexample());
    }

    @Override
    protected boolean testCounterexampleOutput(TransducerResponseDTO counterexample) {
        return counterexample.output().equals(Arrays.asList("-", "5ct!", "coffee!"));
    }
}
