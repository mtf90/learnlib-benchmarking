package de.learnlib.benchmarking.dto;

import java.util.List;

import de.learnlib.query.DefaultQuery;
import io.avaje.jsonb.Json;

@Json
public record AcceptorResponseDTO(List<String> input, boolean output) {

    public AcceptorResponseDTO(DefaultQuery<String, Boolean> query) {
        this(query.getInput().asList(), query.getOutput());
    }
}
