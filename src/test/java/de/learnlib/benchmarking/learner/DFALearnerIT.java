package de.learnlib.benchmarking.learner;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;

import de.learnlib.benchmarking.ServerLifecycleListener;
import de.learnlib.benchmarking.Util;
import de.learnlib.benchmarking.api.learner.LearnerEndpoint;
import de.learnlib.benchmarking.api.learner.LearnerEndpointDFA;
import de.learnlib.benchmarking.api.learner.LearnerEndpointMealy;
import de.learnlib.benchmarking.dto.AcceptorResponseDTO;
import de.learnlib.benchmarking.dto.NewExperimentOutputDTO;
import de.learnlib.benchmarking.util.cache.ModelServiceDFA;
import de.learnlib.cli.factory.LearnerFactory;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.option.Type;
import de.learnlib.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.util.Experiment;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;

public class DFALearnerIT extends AbstractLearnerIT<DFA<?, String>> {

    private static final String MODEL = "dfa/AngluinDFA.dot";

    private LearnerEndpointDFA learnerEndpointDFA;

    @Factory(dataProviderClass = Util.class, dataProvider = "validDFALearners")
    public DFALearnerIT(Learner learner) {
        super(learner);
    }

    @BeforeClass
    public void beforeConcreteClass() {
        this.learnerEndpointDFA = ServerLifecycleListener.getClient().create(LearnerEndpointDFA.class);
    }

    @Override
    protected Type getType() {
        return Type.DFA;
    }

    @Override
    protected String getModel() {
        return MODEL;
    }

    @Override
    protected Set<String> getInputs() {
        return Set.of("0", "1");
    }

    @Override
    protected LearnerEndpoint<?, ?> getWrongLearnerEndpoint() {
        return ServerLifecycleListener.getClient().create(LearnerEndpointMealy.class);
    }

    @Override
    protected Experiment<DFA<?, String>, String, ?> buildExperiment(NewExperimentOutputDTO experiment,
                                                                    Learner learner) {
        var alphabet = Alphabets.fromCollection(experiment.alphabet());
        var mqo = new AccMQO(learnerEndpointDFA, experiment.id());
        var options = new Options();
        options.learner = learner;
        var l = LearnerFactory.<String>getDFALearner(options).constructLearner(alphabet, mqo);
        return new Experiment<>(l, new EQO(learnerEndpointDFA, experiment), alphabet);
    }

    @Override
    protected boolean checkEquivalence(DFA<?, String> hypothesis, Collection<String> inputs)
            throws IOException, FormatException {

        var expected = ModelServiceDFA.readDFA(MODEL).model;
        return Automata.testEquivalence(hypothesis, expected, inputs);
    }

    private static class EQO implements DFAEquivalenceOracle<String> {

        private final LearnerEndpointDFA endpoint;
        private final NewExperimentOutputDTO experiment;

        private EQO(LearnerEndpointDFA endpoint, NewExperimentOutputDTO experiment) {
            this.endpoint = endpoint;
            this.experiment = experiment;
        }

        @Override
        public @Nullable DefaultQuery<String, Boolean> findCounterExample(DFA<?, String> hypothesis,
                                                                          Collection<? extends String> collection) {
            try (Writer w = new StringWriter()) {
                GraphDOT.write(hypothesis, collection, w);
                AcceptorResponseDTO response = endpoint.findCounterExample(experiment.id(), w.toString());
                if (response == null) {
                    return null;
                } else {
                    Assert.assertNotEquals(response.output(), hypothesis.computeOutput(response.input()));
                    return new DefaultQuery<>(Word.fromList(response.input()), response.output());
                }
            } catch (IOException | FormatException e) {
                Assert.fail("serialization should not fail", e);
            }
            return null;
        }
    }

}
