package de.learnlib.benchmarking.learner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.learnlib.benchmarking.ServerLifecycleListener;
import de.learnlib.benchmarking.Util;
import de.learnlib.benchmarking.api.ExperimentEndpoint;
import de.learnlib.benchmarking.api.StatisticsEndpoint;
import de.learnlib.benchmarking.api.LearnerEndpoint;
import de.learnlib.benchmarking.dto.AcceptorResponseDTO;
import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.dto.NewExperimentInputDTO;
import de.learnlib.benchmarking.dto.NewExperimentOutputDTO;
import de.learnlib.benchmarking.dto.NewTesterExperimentInputDTO;
import de.learnlib.benchmarking.dto.TransducerResponseDTO;
import de.learnlib.benchmarking.exception.ExperimentNotFoundException;
import de.learnlib.benchmarking.exception.TypeMismatchException;
import de.learnlib.cli.option.EQOParams;
import de.learnlib.cli.option.EQOParams.RandomWpMethod;
import de.learnlib.cli.option.EQOracle;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Type;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import de.learnlib.util.Experiment;
import io.avaje.http.client.HttpException;
import io.avaje.jex.http.HttpStatus;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.exception.FormatException;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class AbstractLearnerIT<M extends FiniteRepresentation> {

    private ExperimentEndpoint experimentEndpoint;
    private StatisticsEndpoint statisticsEndpoint;
    private LearnerEndpoint<?, ?> wrongLearnerEndpoint;

    private NewExperimentOutputDTO experiment;
    private final Learner learner;

    public AbstractLearnerIT(Learner learner) {
        this.learner = learner;
    }

    @BeforeClass // don't override in child
    public final void beforeAbstractClass() {
        this.experimentEndpoint = ServerLifecycleListener.getClient().create(ExperimentEndpoint.class);
        this.statisticsEndpoint = ServerLifecycleListener.getClient().create(StatisticsEndpoint.class);
        this.wrongLearnerEndpoint = getWrongLearnerEndpoint();
    }

    protected abstract Type getType();

    protected abstract String getModel();

    protected abstract Set<String> getInputs();

    protected abstract LearnerEndpoint<?, ?> getWrongLearnerEndpoint();

    protected abstract Experiment<M, String, ?> buildExperiment(NewExperimentOutputDTO experiment, Learner learner);

    protected abstract boolean checkEquivalence(M hypothesis, Collection<String> inputs)
            throws IOException, FormatException;

    @Test
    public void testCreateMismatchedExperiment() {
        var mismatchedType = Util.getMismatchedType(getType());
        var config = buildConfig();

        Assert.assertThrows(HttpException.class,
                            () -> experimentEndpoint.createForLearner(new NewExperimentInputDTO(mismatchedType,
                                                                                                getModel(),
                                                                                                config)));
    }

    @Test
    public void testCreateExperiment() throws IOException, FormatException {
        var config = buildConfig();
        var experiment = experimentEndpoint.createForLearner(new NewExperimentInputDTO(getType(), getModel(), config));

        Assert.assertNotNull(experiment);
        Assert.assertEquals(new HashSet<>(experiment.alphabet()), getInputs());
        Assert.assertTrue(experiment.id() > 0);

        this.experiment = experiment;
    }

    @Test(dependsOnMethods = "testCreateExperiment")
    public void testTypeMismatch() {
        var e1 = Assert.expectThrows(HttpException.class,
                                     () -> wrongLearnerEndpoint.answerQueries(experiment.id(),
                                                                              Collections.singletonList(Collections.singletonList(
                                                                                      experiment.alphabet()
                                                                                                .getFirst()))));
        Assert.assertEquals(e1.statusCode(), HttpStatus.BAD_REQUEST_400.status());
        Assert.assertEquals(e1.bodyAsString(), TypeMismatchException.MSG);

        var e2 = Assert.expectThrows(HttpException.class,
                                     () -> wrongLearnerEndpoint.findCounterExample(experiment.id(), ""));
        Assert.assertEquals(e2.statusCode(), HttpStatus.BAD_REQUEST_400.status());
        Assert.assertEquals(e2.bodyAsString(), TypeMismatchException.MSG);
    }

    @Test(dependsOnMethods = "testCreateExperiment")
    public void testKindMismatch() throws IOException, FormatException {
        var testerExperiment =
                experimentEndpoint.createForTester(new NewTesterExperimentInputDTO(getType(), getModel(), Learner.TTT));

        var e1 = Assert.expectThrows(HttpException.class,
                                     () -> wrongLearnerEndpoint.answerQueries(testerExperiment.id(),
                                                                              Collections.singletonList(Collections.singletonList(
                                                                                      testerExperiment.alphabet()
                                                                                                      .getFirst()))));
        Assert.assertEquals(e1.statusCode(), HttpStatus.NOT_FOUND_404.status());
        Assert.assertEquals(e1.bodyAsString(), ExperimentNotFoundException.MSG);

        var e2 = Assert.expectThrows(HttpException.class,
                                     () -> wrongLearnerEndpoint.findCounterExample(testerExperiment.id(), ""));
        Assert.assertEquals(e2.statusCode(), HttpStatus.NOT_FOUND_404.status());
        Assert.assertEquals(e2.bodyAsString(), ExperimentNotFoundException.MSG);
    }

    @Test(dependsOnMethods = "testCreateExperiment")
    public void testRunExperiment() throws IOException, FormatException {
        var inputs = experiment.alphabet();
        var exp = buildExperiment(experiment, learner);

        exp.run();

        var hypothesis = exp.getFinalHypothesis();

        Assert.assertTrue(checkEquivalence(hypothesis, inputs));
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

    protected static class AccMQO implements MembershipOracle<String, Boolean> {

        private final LearnerEndpoint<List<Boolean>, AcceptorResponseDTO> endpoint;
        private final long experimentId;

        AccMQO(LearnerEndpoint<List<Boolean>, AcceptorResponseDTO> endpoint, long experimentId) {
            this.endpoint = endpoint;
            this.experimentId = experimentId;
        }

        @Override
        public void processQueries(Collection<? extends Query<String, Boolean>> collection) {

            final List<List<String>> queries = new ArrayList<>(collection.size());

            for (Query<String, Boolean> q : collection) {
                queries.add(q.getInput().asList());
            }

            final List<Boolean> results = endpoint.answerQueries(experimentId, queries);

            var qIter = collection.iterator();
            var rIter = results.iterator();

            while (qIter.hasNext() && rIter.hasNext()) {
                qIter.next().answer(rIter.next());
            }

            Assert.assertFalse(qIter.hasNext());
            Assert.assertFalse(rIter.hasNext());
        }
    }

    protected static class TransMQO implements MembershipOracle<String, Word<String>> {

        private final LearnerEndpoint<List<List<String>>, TransducerResponseDTO> endpoint;
        private final long experimentId;

        TransMQO(LearnerEndpoint<List<List<String>>, TransducerResponseDTO> endpoint, long experimentId) {
            this.endpoint = endpoint;
            this.experimentId = experimentId;
        }

        @Override
        public void processQueries(Collection<? extends Query<String, Word<String>>> collection) {

            final List<List<String>> queries = new ArrayList<>(collection.size());

            for (Query<String, Word<String>> q : collection) {
                queries.add(q.getInput().asList());
            }

            final List<List<String>> results = endpoint.answerQueries(experimentId, queries);

            var qIter = collection.iterator();
            var rIter = results.iterator();

            while (qIter.hasNext() && rIter.hasNext()) {
                var q = qIter.next();
                var r = rIter.next();
                var answer = r.subList(q.getPrefix().length(), r.size());
                q.answer(Word.fromList(answer));
            }

            Assert.assertFalse(qIter.hasNext());
            Assert.assertFalse(rIter.hasNext());
        }
    }
}
