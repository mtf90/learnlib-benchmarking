package de.learnlib.benchmarking.mixin;

import de.learnlib.cli.option.EQOParams;
import de.learnlib.cli.option.EQOParams.CLIMethod;
import io.avaje.jsonb.Json.Ignore;
import io.avaje.jsonb.Json.MixIn;

@MixIn(EQOParams.class)
public class EQOParamsMixin {

    @Ignore
    CLIMethod cliMethod;

}
