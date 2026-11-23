package de.learnlib.benchmarking.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import de.learnlib.benchmarking.ServerLifecycleListener;
import de.learnlib.benchmarking.Util;
import de.learnlib.benchmarking.api.EquivalenceEndpoint;
import de.learnlib.benchmarking.api.ExperimentEndpoint;
import de.learnlib.benchmarking.api.StatisticsEndpoint;
import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.dto.NewExperimentOutputDTO;
import de.learnlib.benchmarking.dto.NewRunnerExperimentInputDTO;
import de.learnlib.benchmarking.exception.ExperimentNotYetFinishedException;
import de.learnlib.cli.option.EQOParams;
import de.learnlib.cli.option.EQOParams.RandomWMethod;
import de.learnlib.cli.option.EQOParams.RandomWpMethod;
import de.learnlib.cli.option.EQOParams.WMethod;
import de.learnlib.cli.option.EQOParams.WpMethod;
import de.learnlib.cli.option.EQOracle;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Type;
import io.avaje.http.client.HttpException;
import io.avaje.jex.http.HttpStatus;
import net.automatalib.exception.FormatException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public abstract class AbstractRunnerIT {

    private ExperimentEndpoint experimentEndpoint;
    private StatisticsEndpoint statisticsEndpoint;
    private EquivalenceEndpoint equivalenceEndpoint;

    private NewExperimentOutputDTO experiment;

    @BeforeClass
    public void beforeClass() {
        this.experimentEndpoint = ServerLifecycleListener.getClient().create(ExperimentEndpoint.class);
        this.statisticsEndpoint = ServerLifecycleListener.getClient().create(StatisticsEndpoint.class);
        this.equivalenceEndpoint = ServerLifecycleListener.getClient().create(EquivalenceEndpoint.class);
    }

    @DataProvider(name = "configs")
    public Object[][] getConfigs() {
        var learners = EnumSet.allOf(Learner.class);
        learners.remove(Learner.L_STAR); // L* is too quick because it requires no counterexamples
        var oracles = EnumSet.of(EQOracle.RANDOM_WP, EQOracle.RANDOM_W, EQOracle.WP, EQOracle.W);

        var options = new Object[learners.size() * oracles.size()][];
        var idx = 0;

        for (Learner l : learners) {
            for (EQOracle o : oracles) {
                options[idx++] = new Object[] {l, o};
            }
        }

        return options;
    }

    @Test(dataProvider = "configs")
    public void testCreateMismatchedExperiment(Learner learner, EQOracle eqo) {
        var mismatchedType = Util.getMismatchedType(getType());
        var config = buildConfig(eqo);

        Assert.assertThrows(() -> experimentEndpoint.createForRunner(new NewRunnerExperimentInputDTO(mismatchedType,
                                                                                                     getModel(),
                                                                                                     learner,
                                                                                                     config)));
    }

    @Test(dataProvider = "configs")
    public void testCreateExperiment(Learner learner, EQOracle eqo)
            throws IOException, FormatException, InterruptedException {
        var config = buildConfig(eqo);

        if (isLearnerSupported(learner)) {
            this.experiment = experimentEndpoint.createForRunner(new NewRunnerExperimentInputDTO(getType(),
                                                                                                 getModel(),
                                                                                                 learner,
                                                                                                 config));
            Assert.assertNotNull(experiment);

            // check statistics immediately after to force a NotYetFinishedExceptions
            var e = Assert.expectThrows(HttpException.class, () -> statisticsEndpoint.getStatistics(experiment.id()));

            Assert.assertEquals(e.statusCode(), HttpStatus.CONFLICT_409.status());
            Assert.assertEquals(e.bodyAsString(), ExperimentNotYetFinishedException.MSG);

            Assert.assertEquals(new HashSet<>(experiment.alphabet()), getInputSymbols());
            Assert.assertTrue(experiment.id() > 0);

            Thread.sleep(50);

            var statistics = statisticsEndpoint.getStatistics(experiment.id());

            Assert.assertNotNull(statistics);
            Assert.assertTrue(statistics.numberOfLearnerQueries() > 0);
            Assert.assertTrue(statistics.numberOfLearnerSymbols() > 0);
            Assert.assertTrue(statistics.numberOfTesterQueries() > 0);
            Assert.assertTrue(statistics.numberOfTesterSymbols() > 0);
        } else {
            var e = Assert.expectThrows(HttpException.class,
                                        () -> experimentEndpoint.createForRunner(new NewRunnerExperimentInputDTO(getType(),
                                                                                                                 getModel(),
                                                                                                                 learner,
                                                                                                                 config)));
            // TODO correctly handle UnsupportedCombinationException();
        }
    }

    @Test(dependsOnMethods = "testCreateExperiment", dataProvider = "configs", enabled = false)
    public void testFinalEquivalence(Learner learner, EQOracle eqo) throws IOException, FormatException {
        if (isLearnerSupported(learner)) {
            // TODO this currently makes no sense. we need a way to fetch the final hypothesis of the run as well
            Assert.assertTrue(equivalenceEndpoint.testEquivalence(experiment.id(),
                                                                  Files.readString(Path.of("systems")
                                                                                       .resolve(getModel()))));
        }
    }

    protected abstract String getModel();

    protected abstract Type getType();

    protected abstract Set<String> getInputSymbols();

    protected abstract boolean isLearnerSupported(Learner learner);

    protected EQOConfig buildConfig(EQOracle oracle) {
        var eqos = Collections.singletonList(oracle);
        var eqoParams = new EQOParams();
        eqoParams.randomWpMethod = new RandomWpMethod();
        eqoParams.randomWpMethod.bound = 10000;
        eqoParams.randomWpMethod.minimalSize = 3;
        eqoParams.randomWpMethod.rndLength = 4;
        eqoParams.randomWpMethod.seed = 0;
        eqoParams.randomWMethod = new RandomWMethod();
        eqoParams.randomWMethod.bound = 10000;
        eqoParams.randomWMethod.minimalSize = 3;
        eqoParams.randomWMethod.rndLength = 4;
        eqoParams.randomWMethod.seed = 0;
        eqoParams.wpMethod = new WpMethod();
        eqoParams.wpMethod.expectedSize = 3;
        eqoParams.wpMethod.lookahead = 1;
        eqoParams.wMethod = new WMethod();
        eqoParams.wMethod.expectedSize = 3;
        eqoParams.wMethod.lookahead = 1;

        return new EQOConfig(eqos, eqoParams);
    }
}
