package de.learnlib.benchmarking.dto;

import java.time.ZonedDateTime;

import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.ModelType;
import io.avaje.jsonb.Json;

@Json
public record ExperimentDTO(long id, ModelType type, String model, ZonedDateTime timestamp) {

    public ExperimentDTO(DBExperiment experiment) {
        this(experiment.getId(), experiment.getType(), experiment.getModel(), experiment.getTimestamp());
    }
}
