package de.learnlib.benchmarking.mixin;

import de.learnlib.cli.option.EQOracle;
import io.avaje.jsonb.Json.Ignore;
import io.avaje.jsonb.Json.MixIn;

@MixIn(EQOracle.class)
public enum EQOracleMixin {

    @Ignore
    CLI

}
