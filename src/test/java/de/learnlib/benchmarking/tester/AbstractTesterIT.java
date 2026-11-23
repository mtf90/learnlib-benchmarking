package de.learnlib.benchmarking.tester;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import de.learnlib.benchmarking.ServerLifecycleListener;
import de.learnlib.benchmarking.Util;
import de.learnlib.benchmarking.api.EquivalenceEndpoint;
import de.learnlib.benchmarking.api.ExperimentEndpoint;
import de.learnlib.benchmarking.api.StatisticsEndpoint;
import de.learnlib.benchmarking.api.tester.TesterEndpoint;
import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.dto.NewExperimentInputDTO;
import de.learnlib.benchmarking.dto.NewExperimentOutputDTO;
import de.learnlib.benchmarking.dto.NewTesterExperimentInputDTO;
import de.learnlib.benchmarking.exception.ExperimentNotFoundException;
import de.learnlib.benchmarking.exception.TypeMismatchException;
import de.learnlib.cli.option.EQOParams;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Type;
import io.avaje.http.client.HttpException;
import io.avaje.jex.http.HttpStatus;
import net.automatalib.exception.FormatException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class AbstractTesterIT<CEOUT> {

    private ExperimentEndpoint experimentEndpoint;
    private StatisticsEndpoint statisticsEndpoint;
    private EquivalenceEndpoint equivalenceEndpoint;

    private TesterEndpoint<CEOUT> testerEndpointModel;
    private TesterEndpoint<?> testerEndpointOther;

    private NewExperimentOutputDTO experiment;

    @BeforeClass // don't override in child
    public final void beforeAbstractClass() {
        this.experimentEndpoint = ServerLifecycleListener.getClient().create(ExperimentEndpoint.class);
        this.statisticsEndpoint = ServerLifecycleListener.getClient().create(StatisticsEndpoint.class);
        this.equivalenceEndpoint = ServerLifecycleListener.getClient().create(EquivalenceEndpoint.class);

        this.testerEndpointModel = getLearnerEndpointModel();
        this.testerEndpointOther = getLearnerEndpointOther();
    }

    protected abstract TesterEndpoint<CEOUT> getLearnerEndpointModel();

    protected abstract TesterEndpoint<?> getLearnerEndpointOther();

    protected abstract Type getType();

    protected abstract String getModel();

    protected abstract Set<String> getInputs();

    protected abstract String getInitialHypothesis() throws IOException;

    protected abstract String getFinalHypothesis() throws IOException;

    protected abstract List<List<String>> getNonCounterexamples();

    protected abstract List<String> getCounterexample();

    protected abstract boolean testCounterexampleInput(CEOUT counterexample);

    protected abstract boolean testCounterexampleOutput(CEOUT counterexample);

    @Test
    public void testCreateMismatchedExperiment() {
        Assert.assertThrows(() -> experimentEndpoint.createForTester(new NewTesterExperimentInputDTO(Util.getMismatchedType(
                getType()), getModel(), Learner.TTT)));
    }

    @Test
    public void testCreateExperiment() throws IOException, FormatException {
        var experiment =
                experimentEndpoint.createForTester(new NewTesterExperimentInputDTO(getType(), getModel(), Learner.TTT));

        Assert.assertNotNull(experiment);
        Assert.assertEquals(new HashSet<>(experiment.alphabet()), getInputs());
        Assert.assertTrue(experiment.id() > 0);

        this.experiment = experiment;
    }

    @Test(dependsOnMethods = "testCreateExperiment")
    public void testTypeMismatch() {
        var e2 = Assert.expectThrows(HttpException.class,
                                     () -> testerEndpointOther.checkForCounterexamples(experiment.id(),
                                                                                       Collections.emptyList()));
        Assert.assertEquals(e2.statusCode(), HttpStatus.BAD_REQUEST_400.status());
        Assert.assertEquals(e2.bodyAsString(), TypeMismatchException.MSG);
    }

    @Test(dependsOnMethods = "testCreateExperiment")
    public void testKindMismatch() throws IOException, FormatException {
        var learnerExperiment = experimentEndpoint.createForLearner(new NewExperimentInputDTO(getType(),
                                                                                              getModel(),
                                                                                              new EQOConfig(Collections.emptyList(),
                                                                                                            new EQOParams())));

        var e1 = Assert.expectThrows(HttpException.class,
                                     () -> testerEndpointOther.getHypothesis(learnerExperiment.id()));
        Assert.assertEquals(e1.statusCode(), HttpStatus.NOT_FOUND_404.status());
        Assert.assertEquals(e1.bodyAsString(), ExperimentNotFoundException.MSG);

        var e2 = Assert.expectThrows(HttpException.class,
                                     () -> testerEndpointOther.checkForCounterexamples(learnerExperiment.id(),
                                                                                       Collections.emptyList()));
        Assert.assertEquals(e2.statusCode(), HttpStatus.NOT_FOUND_404.status());
        Assert.assertEquals(e2.bodyAsString(), ExperimentNotFoundException.MSG);
    }

    @Test(dependsOnMethods = "testCreateExperiment")
    public void testFetchInitialHypothesis() throws IOException, ExecutionException, InterruptedException {
        final var hypothesis = testerEndpointModel.getHypothesis(experiment.id());

        Assert.assertNotNull(hypothesis);
        Assert.assertEquals(hypothesis, getInitialHypothesis());

        final var statistics = statisticsEndpoint.getStatistics(experiment.id());

        Assert.assertTrue(statistics.numberOfLearnerQueries() > 0);
        Assert.assertTrue(statistics.numberOfLearnerSymbols() > 0);
        Assert.assertEquals(statistics.numberOfTesterQueries(), 0);
        Assert.assertEquals(statistics.numberOfTesterSymbols(), 0);
    }

    @Test(dependsOnMethods = "testFetchInitialHypothesis")
    public void testInitialEquivalence() throws IOException, FormatException {
        Assert.assertFalse(equivalenceEndpoint.testEquivalence(experiment.id(), getInitialHypothesis()));
    }

    @Test(dependsOnMethods = "testInitialEquivalence")
    public void testPostQueries() throws ExecutionException, InterruptedException, IOException {

        var statistics = statisticsEndpoint.getStatistics(experiment.id());

        final var oldLearnerQueries = statistics.numberOfLearnerQueries();
        final var oldLearnerSymbols = statistics.numberOfLearnerSymbols();

        var nonCEs = getNonCounterexamples();
        var newNumberOfTesterQueries = 0;
        var newNumberOfTesterSymbols = 0;

        for (List<String> nonCE : nonCEs) {
            newNumberOfTesterQueries++;
            newNumberOfTesterSymbols += nonCE.size();
        }

        var counterexample = testerEndpointModel.checkForCounterexamples(experiment.id(), nonCEs);

        Assert.assertNull(counterexample);

        statistics = statisticsEndpoint.getStatistics(experiment.id());

        Assert.assertEquals(statistics.numberOfLearnerQueries(), oldLearnerQueries);
        Assert.assertEquals(statistics.numberOfLearnerSymbols(), oldLearnerSymbols);
        Assert.assertEquals(statistics.numberOfTesterQueries(), newNumberOfTesterQueries);
        Assert.assertEquals(statistics.numberOfTesterSymbols(), newNumberOfTesterSymbols);

        final var inputs = getCounterexample();
        counterexample = testerEndpointModel.checkForCounterexamples(experiment.id(),
                                                                     Arrays.asList(nonCEs.getFirst(),
                                                                                   inputs,
                                                                                   nonCEs.get(1)));

        Assert.assertNotNull(counterexample);
        Assert.assertTrue(testCounterexampleInput(counterexample));
        Assert.assertTrue(testCounterexampleOutput(counterexample));

        // query hyp here, so that refinement has finished and stats have been committed.
        final var hypothesis = testerEndpointModel.getHypothesis(experiment.id());

        Assert.assertNotNull(hypothesis);
        Assert.assertEquals(hypothesis, getFinalHypothesis());

        statistics = statisticsEndpoint.getStatistics(experiment.id());

        Assert.assertTrue(statistics.numberOfLearnerQueries() > oldLearnerQueries);
        Assert.assertTrue(statistics.numberOfLearnerSymbols() > oldLearnerSymbols);
        Assert.assertEquals(statistics.numberOfTesterQueries(), newNumberOfTesterQueries + 2);
        Assert.assertEquals(statistics.numberOfTesterSymbols(),
                            newNumberOfTesterSymbols + nonCEs.getFirst().size() + inputs.size());
    }

    @Test(dependsOnMethods = "testPostQueries")
    public void testFinalEquivalence() throws IOException, FormatException {
        Assert.assertTrue(equivalenceEndpoint.testEquivalence(experiment.id(), getFinalHypothesis()));
    }
}
