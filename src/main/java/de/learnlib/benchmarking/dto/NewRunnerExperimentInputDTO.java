package de.learnlib.benchmarking.dto;

import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Type;
import io.avaje.jsonb.Json;

@Json
public record NewRunnerExperimentInputDTO(Type type, String model, Learner learner, EQOConfig eqoConfig) {}
