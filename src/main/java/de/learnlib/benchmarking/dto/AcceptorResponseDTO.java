package de.learnlib.benchmarking.dto;

import java.util.List;

import io.avaje.jsonb.Json;

@Json
public record AcceptorResponseDTO(List<String> input, boolean output) {}
