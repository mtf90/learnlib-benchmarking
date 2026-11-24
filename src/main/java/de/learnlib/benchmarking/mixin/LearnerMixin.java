package de.learnlib.benchmarking.mixin;

import de.learnlib.cli.option.Learner;
import io.avaje.jsonb.Json.Ignore;
import io.avaje.jsonb.Json.MixIn;

@MixIn(Learner.class)
public enum LearnerMixin {

    @Ignore
    L_SHARP,

    @Ignore
    ADT

}
