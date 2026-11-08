package de.learnlib.benchmarking.dto;

import de.learnlib.benchmarking.entity.DBStatistics;
import io.avaje.jsonb.Json;

@Json
public record StatisticsDTO(int numberOfLearnerQueries, int numberOfLearnerSymbols, int numberOfTesterQueries,
                            int numberOfTesterSymbols) {

    public StatisticsDTO(DBStatistics statistics) {
        this(statistics.getNumberOfLearnerQueries(),
             statistics.getNumberOfLearnerSymbols(),
             statistics.getNumberOfTesterQueries(),
             statistics.getNumberOfTesterSymbols());
    }
}
