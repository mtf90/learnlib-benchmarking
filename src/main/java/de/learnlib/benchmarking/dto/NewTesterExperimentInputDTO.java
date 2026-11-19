package de.learnlib.benchmarking.dto;

import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Type;
import io.avaje.jsonb.Json;

@Json
public record NewTesterExperimentInputDTO(Type type, String model, Learner learner) {}
