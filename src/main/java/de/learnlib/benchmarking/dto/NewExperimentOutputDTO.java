package de.learnlib.benchmarking.dto;

import io.avaje.jsonb.Json;

@Json
public record NewExperimentOutputDTO(long id, int alphabetSize) {}
