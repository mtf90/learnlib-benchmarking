package de.learnlib.benchmarking.runner;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.learnlib.benchmarking.ServerLifecycleListener;
import de.learnlib.benchmarking.api.ExperimentEndpoint;
import de.learnlib.benchmarking.api.StatisticsEndpoint;
import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.dto.NewRunnerExperimentInputDTO;
import de.learnlib.benchmarking.exception.ExperimentNotYetFinishedException;
import de.learnlib.cli.option.EQOParams;
import de.learnlib.cli.option.EQOParams.RandomWpMethod;
import de.learnlib.cli.option.EQOracle;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Type;
import io.avaje.http.client.HttpException;
import io.avaje.jex.http.HttpStatus;
import net.automatalib.exception.FormatException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DFARunnerIT {

    private static final String MODEL = "dfa/AngluinDFA.dot";

    private ExperimentEndpoint experimentEndpoint;
    private StatisticsEndpoint statisticsEndpoint;

    @BeforeClass
    public void beforeClass() {
        this.experimentEndpoint = ServerLifecycleListener.getClient().create(ExperimentEndpoint.class);
        this.statisticsEndpoint = ServerLifecycleListener.getClient().create(StatisticsEndpoint.class);
    }

    @Test
    public void testCreateMismatchedExperiment() {
        var config = buildConfig();
        Assert.assertThrows(() -> experimentEndpoint.createForRunner(new NewRunnerExperimentInputDTO(Type.MEALY,
                                                                                                     MODEL,
                                                                                                     Learner.TTT,
                                                                                                     config)));
    }

    @Test
    public void testCreateExperiment() throws IOException, FormatException, InterruptedException {
        var config = buildConfig();
        var experiment = experimentEndpoint.createForRunner(new NewRunnerExperimentInputDTO(Type.DFA,
                                                                                            MODEL,
                                                                                            Learner.TTT,
                                                                                            config));
        Assert.assertNotNull(experiment);

        // check statistics immediately after to force a NotYetFinishedExceptions
        var e = Assert.expectThrows(HttpException.class, () -> statisticsEndpoint.getStatistics(experiment.id()));

        Assert.assertEquals(e.statusCode(), HttpStatus.CONFLICT_409.status());
        Assert.assertEquals(e.bodyAsString(), ExperimentNotYetFinishedException.MSG);

        Assert.assertEquals(new HashSet<>(experiment.alphabet()), Set.of("0", "1"));
        Assert.assertTrue(experiment.id() > 0);

        Thread.sleep(500);

        var statistics = statisticsEndpoint.getStatistics(experiment.id());

        Assert.assertNotNull(statistics);
        Assert.assertTrue(statistics.numberOfLearnerQueries() > 0);
        Assert.assertTrue(statistics.numberOfLearnerSymbols() > 0);
        Assert.assertTrue(statistics.numberOfTesterQueries() > 0);
        Assert.assertTrue(statistics.numberOfTesterSymbols() > 0);
    }

    private static EQOConfig buildConfig() {
        var eqos = Collections.singletonList(EQOracle.RANDOM_WP);
        var eqoParams = new EQOParams();
        eqoParams.randomWpMethod = new RandomWpMethod();
        eqoParams.randomWpMethod.bound = 10000;
        eqoParams.randomWpMethod.minimalSize = 3;
        eqoParams.randomWpMethod.rndLength = 3;
        eqoParams.randomWpMethod.seed = 0;

        return new EQOConfig(eqos, eqoParams);
    }
}
