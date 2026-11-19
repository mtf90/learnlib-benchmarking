package de.learnlib.benchmarking.tester;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import de.learnlib.benchmarking.ServerLifecycleListener;
import de.learnlib.benchmarking.api.ExperimentEndpoint;
import de.learnlib.benchmarking.api.StatisticsEndpoint;
import de.learnlib.benchmarking.api.tester.TesterEndpointDFA;
import de.learnlib.benchmarking.api.tester.TesterEndpointMealy;
import de.learnlib.benchmarking.dto.NewExperimentOutputDTO;
import de.learnlib.benchmarking.dto.NewTesterExperimentInputDTO;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Type;
import net.automatalib.exception.FormatException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DFATesterIT {

    private static final String MODEL = "dfa/AngluinDFA.dot";

    private ExperimentEndpoint experimentEndpoint;
    private TesterEndpointDFA testerEndpointDFA;
    private TesterEndpointMealy testerEndpointMealy;
    private StatisticsEndpoint statisticsEndpoint;

    private NewExperimentOutputDTO experiment;

    @BeforeClass
    public void beforeClass() {
        this.experimentEndpoint = ServerLifecycleListener.getClient().create(ExperimentEndpoint.class);
        this.testerEndpointDFA = ServerLifecycleListener.getClient().create(TesterEndpointDFA.class);
        this.testerEndpointMealy = ServerLifecycleListener.getClient().create(TesterEndpointMealy.class);
        this.statisticsEndpoint = ServerLifecycleListener.getClient().create(StatisticsEndpoint.class);
    }

    @Test
    public void testCreateMismatchedExperiment() {
        Assert.assertThrows(() -> experimentEndpoint.createForTester(new NewTesterExperimentInputDTO(Type.MEALY,
                                                                                                     MODEL,
                                                                                                     Learner.TTT)));
    }

    @Test
    public void testCreateExperiment() throws IOException, FormatException {
        var experiment =
                experimentEndpoint.createForTester(new NewTesterExperimentInputDTO(Type.DFA, MODEL, Learner.TTT));

        Assert.assertNotNull(experiment);
        Assert.assertEquals(new HashSet<>(experiment.alphabet()), Set.of("0", "1"));
        Assert.assertTrue(experiment.id() > 0);

        this.experiment = experiment;
    }

    @Test(dependsOnMethods = "testCreateExperiment")
    public void testTypeMismatch() {
        Assert.assertThrows(() -> testerEndpointMealy.getHypothesis(experiment.id()));
        Assert.assertThrows(() -> testerEndpointMealy.checkForCounterexamples(experiment.id(),
                                                                              Collections.emptyList()));
    }

    @Test(dependsOnMethods = "testCreateExperiment")
    public void testFetchInitialHypothesis() throws IOException, ExecutionException, InterruptedException {
        final var hypothesis = testerEndpointDFA.getHypothesis(experiment.id());

        Assert.assertNotNull(hypothesis);

        try (var is = DFATesterIT.class.getResourceAsStream("/init_dfa.dot")) {
            Assert.assertNotNull(is);
            Assert.assertEquals(hypothesis, new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }

        final var statistics = statisticsEndpoint.getStatistics(experiment.id());

        Assert.assertTrue(statistics.numberOfLearnerQueries() > 0);
        Assert.assertTrue(statistics.numberOfLearnerSymbols() > 0);
        Assert.assertEquals(statistics.numberOfTesterQueries(), 0);
        Assert.assertEquals(statistics.numberOfTesterSymbols(), 0);
    }

    @Test(dependsOnMethods = "testFetchInitialHypothesis")
    public void testPostQueries() throws ExecutionException, InterruptedException, IOException {

        var statistics = statisticsEndpoint.getStatistics(experiment.id());

        final var oldLearnerQueries = statistics.numberOfLearnerQueries();
        final var oldLearnerSymbols = statistics.numberOfLearnerSymbols();

        var counterexample = testerEndpointDFA.checkForCounterexamples(experiment.id(),
                                                                       Arrays.asList(List.of("0", "1", "1", "1"),
                                                                                     List.of("0", "1", "1", "1", "1")));

        Assert.assertNull(counterexample);

        statistics = statisticsEndpoint.getStatistics(experiment.id());

        Assert.assertEquals(statistics.numberOfLearnerQueries(), oldLearnerQueries);
        Assert.assertEquals(statistics.numberOfLearnerSymbols(), oldLearnerSymbols);
        Assert.assertEquals(statistics.numberOfTesterQueries(), 2);
        Assert.assertEquals(statistics.numberOfTesterSymbols(), 9);

        final var inputs = Arrays.asList("0", "1", "0", "1");
        counterexample = testerEndpointDFA.checkForCounterexamples(experiment.id(),
                                                                   Arrays.asList(List.of("0", "1", "1", "1"),
                                                                                 inputs,
                                                                                 List.of("0", "1", "1", "1", "1")));

        Assert.assertNotNull(counterexample);
        Assert.assertEquals(counterexample.input(), inputs);
        Assert.assertTrue(counterexample.output());

        // query hyp here, so that refinement has finished and stats have been committed.
        final var hypothesis = testerEndpointDFA.getHypothesis(experiment.id());

        Assert.assertNotNull(hypothesis);

        try (var is = DFATesterIT.class.getResourceAsStream("/final_dfa.dot")) {
            Assert.assertNotNull(is);
            Assert.assertEquals(hypothesis, new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }

        statistics = statisticsEndpoint.getStatistics(experiment.id());

        Assert.assertTrue(statistics.numberOfLearnerQueries() > oldLearnerQueries);
        Assert.assertTrue(statistics.numberOfLearnerSymbols() > oldLearnerSymbols);
        Assert.assertEquals(statistics.numberOfTesterQueries(), 5);
        Assert.assertEquals(statistics.numberOfTesterSymbols(), 22);
    }

    @Test(dependsOnMethods = "testPostQueries")
    public void testNoMoreCounterexamples() {
        Assert.assertThrows(() -> testerEndpointDFA.checkForCounterexamples(experiment.id(),
                                                                            Collections.singletonList(Collections.nCopies(
                                                                                    4,
                                                                                    "0"))));
    }
}
