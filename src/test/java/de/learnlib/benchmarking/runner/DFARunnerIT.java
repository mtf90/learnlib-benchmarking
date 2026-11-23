package de.learnlib.benchmarking.runner;

import java.util.Set;

import de.learnlib.benchmarking.Util;
import de.learnlib.cli.factory.LearnerFactory;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.option.Type;

public class DFARunnerIT extends AbstractRunnerIT {

    @Override
    protected String getModel() {
        return "dfa/AngluinDFA.dot";
    }

    @Override
    protected Type getType() {
        return Type.DFA;
    }

    @Override
    protected Set<String> getInputSymbols() {
        return Set.of("0", "1");
    }

    @Override
    protected boolean isLearnerSupported(Learner learner) {
        return Util.isValidDFALearner(learner);
    }

}
