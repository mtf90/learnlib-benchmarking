package de.learnlib.benchmarking.learner;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.learnlib.algorithm.ttt.dfa.TTTLearnerDFA;
import de.learnlib.benchmarking.ServerLifecycleListener;
import de.learnlib.benchmarking.api.ExperimentEndpoint;
import de.learnlib.benchmarking.api.StatisticsEndpoint;
import de.learnlib.benchmarking.api.learner.LearnerEndpointDFA;
import de.learnlib.benchmarking.api.learner.LearnerEndpointMealy;
import de.learnlib.benchmarking.dto.AcceptorResponseDTO;
import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.dto.NewExperimentInputDTO;
import de.learnlib.benchmarking.dto.NewExperimentOutputDTO;
import de.learnlib.benchmarking.dto.NewTesterExperimentInputDTO;
import de.learnlib.benchmarking.exception.ExperimentNotFoundException;
import de.learnlib.benchmarking.exception.TypeMismatchException;
import de.learnlib.benchmarking.util.cache.ModelCacheDFA;
import de.learnlib.cli.option.EQOParams;
import de.learnlib.cli.option.EQOParams.RandomWpMethod;
import de.learnlib.cli.option.EQOracle;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Type;
import de.learnlib.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import de.learnlib.util.Experiment;
import io.avaje.http.client.HttpException;
import io.avaje.jex.http.HttpStatus;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DFALearnerIT {

    private static final String MODEL = "dfa/AngluinDFA.dot";

    private ExperimentEndpoint experimentEndpoint;
    private LearnerEndpointDFA learnerEndpointDFA;
    private LearnerEndpointMealy learnerEndpointMealy;
    private StatisticsEndpoint statisticsEndpoint;

    private NewExperimentOutputDTO experiment;

    @BeforeClass
    public void beforeClass() {
        this.experimentEndpoint = ServerLifecycleListener.getClient().create(ExperimentEndpoint.class);
        this.learnerEndpointDFA = ServerLifecycleListener.getClient().create(LearnerEndpointDFA.class);
        this.learnerEndpointMealy = ServerLifecycleListener.getClient().create(LearnerEndpointMealy.class);
        this.statisticsEndpoint = ServerLifecycleListener.getClient().create(StatisticsEndpoint.class);
    }

    @Test
    public void testCreateMismatchedExperiment() {
        var config = buildConfig();
        Assert.assertThrows(() -> experimentEndpoint.createForLearner(new NewExperimentInputDTO(Type.MEALY,
                                                                                                MODEL,
                                                                                                config)));
    }

    @Test
    public void testCreateExperiment() throws IOException, FormatException {
        var config = buildConfig();
        var experiment = experimentEndpoint.createForLearner(new NewExperimentInputDTO(Type.DFA, MODEL, config));

        Assert.assertNotNull(experiment);
        Assert.assertEquals(new HashSet<>(experiment.alphabet()), Set.of("0", "1"));
        Assert.assertTrue(experiment.id() > 0);

        this.experiment = experiment;
    }

    @Test(dependsOnMethods = "testCreateExperiment")
    public void testTypeMismatch() {
        var e1 = Assert.expectThrows(HttpException.class,
                                     () -> learnerEndpointMealy.answerQueries(experiment.id(),
                                                                              Collections.singletonList(Collections.singletonList(
                                                                                      experiment.alphabet()
                                                                                                .getFirst()))));
        Assert.assertEquals(e1.statusCode(), HttpStatus.BAD_REQUEST_400.status());
        Assert.assertEquals(e1.bodyAsString(), TypeMismatchException.MSG);

        var e2 = Assert.expectThrows(HttpException.class,
                                     () -> learnerEndpointMealy.findCounterExample(experiment.id(), ""));
        Assert.assertEquals(e2.statusCode(), HttpStatus.BAD_REQUEST_400.status());
        Assert.assertEquals(e2.bodyAsString(), TypeMismatchException.MSG);
    }

    @Test(dependsOnMethods = "testCreateExperiment")
    public void testKindMismatch() throws IOException, FormatException {
        var testerExperiment =
                experimentEndpoint.createForTester(new NewTesterExperimentInputDTO(Type.DFA, MODEL, Learner.TTT));

        var e1 = Assert.expectThrows(HttpException.class,
                                     () -> learnerEndpointMealy.answerQueries(testerExperiment.id(),
                                                                              Collections.singletonList(Collections.singletonList(
                                                                                      testerExperiment.alphabet()
                                                                                                      .getFirst()))));
        Assert.assertEquals(e1.statusCode(), HttpStatus.NOT_FOUND_404.status());
        Assert.assertEquals(e1.bodyAsString(), ExperimentNotFoundException.MSG);

        var e2 = Assert.expectThrows(HttpException.class,
                                     () -> learnerEndpointMealy.findCounterExample(testerExperiment.id(), ""));
        Assert.assertEquals(e2.statusCode(), HttpStatus.NOT_FOUND_404.status());
        Assert.assertEquals(e2.bodyAsString(), ExperimentNotFoundException.MSG);
    }

    @Test(dependsOnMethods = "testCreateExperiment")
    public void testRunExperiment() throws IOException, FormatException {
        var alphabet = Alphabets.fromCollection(experiment.alphabet());
        var mqo = new MQO();
        var learner = new TTTLearnerDFA<>(alphabet, mqo);
        var eqo = new EQO();

        var experiment = new Experiment<>(learner, eqo, alphabet);

        experiment.run();

        var hypothesis = experiment.getFinalHypothesis();
        var expected = ModelCacheDFA.readDFA(MODEL).model;

        Assert.assertTrue(Automata.testEquivalence(hypothesis, expected, alphabet));
    }

    @Test(dependsOnMethods = "testRunExperiment")
    public void testStatistics() {
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
        eqoParams.randomWpMethod.bound = 100;
        eqoParams.randomWpMethod.minimalSize = 3;
        eqoParams.randomWpMethod.rndLength = 2;
        eqoParams.randomWpMethod.seed = 0;

        return new EQOConfig(eqos, eqoParams);
    }

    private class MQO implements DFAMembershipOracle<String> {

        @Override
        public void processQueries(Collection<? extends Query<String, Boolean>> collection) {

            final List<List<String>> queries = new ArrayList<>(collection.size());

            for (Query<String, Boolean> q : collection) {
                queries.add(q.getInput().asList());
            }

            final List<Boolean> results = learnerEndpointDFA.answerQueries(experiment.id(), queries);

            var qIter = collection.iterator();
            var rIter = results.iterator();

            while (qIter.hasNext() && rIter.hasNext()) {
                qIter.next().answer(rIter.next());
            }

            Assert.assertFalse(qIter.hasNext());
            Assert.assertFalse(rIter.hasNext());
        }
    }

    private class EQO implements DFAEquivalenceOracle<String> {

        @Override
        public @Nullable DefaultQuery<String, Boolean> findCounterExample(DFA<?, String> objects,
                                                                          Collection<? extends String> collection) {
            try (Writer w = new StringWriter()) {
                GraphDOT.write(objects, collection, w);
                AcceptorResponseDTO response = learnerEndpointDFA.findCounterExample(experiment.id(), w.toString());
                if (response == null) {
                    return null;
                } else {
                    return new DefaultQuery<>(Word.fromList(response.input()), response.output());
                }
            } catch (IOException | FormatException e) {
                Assert.fail("serialization should not fail", e);
            }
            return null;
        }
    }

}
