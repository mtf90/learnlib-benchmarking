package de.learnlib.benchmarking.learner;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;

import de.learnlib.benchmarking.ServerLifecycleListener;
import de.learnlib.benchmarking.Util;
import de.learnlib.benchmarking.api.LearnerEndpoint;
import de.learnlib.benchmarking.api.LearnerEndpointDFA;
import de.learnlib.benchmarking.api.LearnerEndpointMealy;
import de.learnlib.benchmarking.dto.NewExperimentOutputDTO;
import de.learnlib.benchmarking.dto.TransducerResponseDTO;
import de.learnlib.benchmarking.util.cache.ModelServiceMealy;
import de.learnlib.cli.factory.LearnerFactory;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.option.Type;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.util.Experiment;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;

public class MealyLearnerIT extends AbstractLearnerIT<MealyMachine<?, String, ?, String>> {

    private static final String MODEL = "mealy/CoffeeMealyMachine.dot";

    private LearnerEndpointMealy learnerEndpointMealy;

    @Factory(dataProviderClass = Util.class, dataProvider = "validMealyLearners")
    public MealyLearnerIT(Learner learner) {
        super(learner);
    }

    @BeforeClass
    public void beforeConcreteClass() {
        this.learnerEndpointMealy = ServerLifecycleListener.getClient().create(LearnerEndpointMealy.class);
    }

    @Override
    protected Type getType() {
        return Type.MEALY;
    }

    @Override
    protected String getModel() {
        return MODEL;
    }

    @Override
    protected Set<String> getInputs() {
        return Set.of("5ct?", "10ct?", "button?");
    }

    @Override
    protected LearnerEndpoint<?, ?> getWrongLearnerEndpoint() {
        return ServerLifecycleListener.getClient().create(LearnerEndpointDFA.class);
    }

    @Override
    protected Experiment<MealyMachine<?, String, ?, String>, String, ?> buildExperiment(NewExperimentOutputDTO experiment,
                                                                                        Learner learner) {
        var alphabet = Alphabets.fromCollection(experiment.alphabet());
        var mqo = new TransMQO(learnerEndpointMealy, experiment.id());
        var options = new Options();
        options.learner = learner;
        var l = LearnerFactory.<String, String>getMealyLearner(options).constructLearner(alphabet, mqo);
        return new Experiment<>(l, new EQO(learnerEndpointMealy, experiment), alphabet);
    }

    @Override
    protected boolean checkEquivalence(MealyMachine<?, String, ?, String> hypothesis, Collection<String> inputs)
            throws IOException, FormatException {

        var expected = ModelServiceMealy.readMealy(MODEL).model;
        return Automata.testEquivalence(hypothesis, expected, inputs);
    }

    private static class EQO implements MealyEquivalenceOracle<String, String> {

        private final LearnerEndpointMealy endpoint;
        private final NewExperimentOutputDTO experiment;

        private EQO(LearnerEndpointMealy endpoint, NewExperimentOutputDTO experiment) {
            this.endpoint = endpoint;
            this.experiment = experiment;
        }

        @Override
        public @Nullable DefaultQuery<String, Word<String>> findCounterExample(MealyMachine<?, String, ?, String> hypothesis,
                                                                               Collection<? extends String> collection) {
            try (Writer w = new StringWriter()) {
                GraphDOT.write(hypothesis, collection, w);
                TransducerResponseDTO response = endpoint.findCounterExample(experiment.id(), w.toString());
                if (response == null) {
                    return null;
                } else {
                    Assert.assertNotEquals(response.output(), hypothesis.computeOutput(response.input()));
                    return new DefaultQuery<>(Word.epsilon(),
                                              Word.fromList(response.input()),
                                              Word.fromList(response.output()));
                }
            } catch (IOException | FormatException e) {
                Assert.fail("serialization should not fail", e);
            }
            return null;
        }
    }

}
