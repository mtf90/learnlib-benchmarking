package de.learnlib.benchmarking.dto;

import de.learnlib.cli.option.Type;
import io.avaje.jsonb.Json;

@Json
public record NewExperimentInputDTO(Type type, String model, EQOConfig eqoConfig) {}
