package de.learnlib.benchmarking.dto;

import java.time.ZonedDateTime;

import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.DBExperiment.Kind;
import de.learnlib.cli.option.Type;
import io.avaje.jsonb.Json;

@Json
public record ExperimentDTO(long id, Kind kind, Type type, String model, ZonedDateTime timestamp) {

    public ExperimentDTO(DBExperiment experiment) {
        this(experiment.getId(),
             experiment.getKind(),
             experiment.getType(),
             experiment.getModel(),
             experiment.getTimestamp());
    }
}
