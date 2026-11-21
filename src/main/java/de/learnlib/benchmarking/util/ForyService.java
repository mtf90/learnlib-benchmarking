package de.learnlib.benchmarking.util;

import org.apache.fory.Fory;
import org.apache.fory.logging.LoggerFactory;
import org.apache.fory.resolver.AllowListChecker;
import org.apache.fory.resolver.AllowListChecker.CheckLevel;

public class ForyService {

    public static final Fory FORY;

    static {
        final AllowListChecker checker = new AllowListChecker();
        checker.setCheckLevel(CheckLevel.STRICT);
        checker.allowClass("de.learnlib.*");
        checker.allowClass("net.automatalib.*");
        FORY = Fory.builder()
                   .requireClassRegistration(false)
                   .withCodegen(false)
                   .withRefTracking(true)
                   .withTypeChecker(checker)
                   .withXlang(false)
                   .build();
        LoggerFactory.useSlf4jLogging(true);
    }

    public static byte[] toBytes(Object state) {
        return FORY.serialize(state);
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromBytes(byte[] bytes) {
        return (T) FORY.deserialize(bytes);
    }
}
