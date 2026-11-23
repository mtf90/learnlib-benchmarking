package de.learnlib.benchmarking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Random;

import de.learnlib.cli.factory.LearnerFactory;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.option.Type;
import net.automatalib.common.util.random.RandomUtil;
import org.testng.annotations.DataProvider;

public class Util {

    public static Type getMismatchedType(Type type) {
        var typeSingleton = EnumSet.of(type);
        var wrongTypes = EnumSet.complementOf(typeSingleton);
        return RandomUtil.choose(new Random(23), new ArrayList<>(wrongTypes));
    }

    @DataProvider(name = "validDFALearners")
    public static Object[] validDFALearners() {
        return Arrays.stream(Learner.values()).filter(Util::isValidDFALearner).toArray();
    }

    @DataProvider(name = "validMealyLearners")
    public static Object[] validMealyLearners() {
        return Arrays.stream(Learner.values()).filter(Util::isValidMealyLearner).toArray();
    }

    public static boolean isValidDFALearner(Learner learner) {
        var options = new Options();
        options.learner = learner;
        options.type = Type.DFA;

        // let the factory decide
        try {
            LearnerFactory.getDFALearner(options);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidMealyLearner(Learner learner) {
        var options = new Options();
        options.learner = learner;
        options.type = Type.MEALY;

        // let the factory decide
        try {
            LearnerFactory.getMealyLearner(options);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
