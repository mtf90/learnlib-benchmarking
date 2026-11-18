package de.learnlib.benchmarking.dto;

import java.util.List;

import io.avaje.jsonb.Json;

@Json
public record TransducerResponseDTO(List<String> input, List<String> output) {}
