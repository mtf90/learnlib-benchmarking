package de.learnlib.benchmarking.runner;

import java.util.Set;

import de.learnlib.benchmarking.Util;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Type;

public class MealyRunnerIT extends AbstractRunnerIT {

    @Override
    protected String getModel() {
        return "mealy/CoffeeMealyMachine.dot";
    }

    @Override
    protected Type getType() {
        return Type.MEALY;
    }

    @Override
    protected Set<String> getInputSymbols() {
        return Set.of("5ct?", "10ct?", "button?");
    }

    @Override
    protected boolean isLearnerSupported(Learner learner) {
        return Util.isValidMealyLearner(learner);
    }

}
