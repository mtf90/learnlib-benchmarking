package de.learnlib.benchmarking.dto;

import de.learnlib.benchmarking.entity.ModelType;
import io.avaje.jsonb.Json;

@Json
public record NewExperimentInputDTO(ModelType type, String model) {}
