package de.learnlib.benchmarking.entity;

import io.avaje.jsonb.Json;
import jakarta.persistence.Entity;

@Entity
@Json
public class DBStatistics extends DBIdentified {

    private int numberOfQueries;

    private int numberOfSymbols;

    public int getNumberOfQueries() {
        return numberOfQueries;
    }

    public void setNumberOfQueries(int numberOfQueries) {
        this.numberOfQueries = numberOfQueries;
    }

    public int getNumberOfSymbols() {
        return numberOfSymbols;
    }

    public void setNumberOfSymbols(int numberOfSymbols) {
        this.numberOfSymbols = numberOfSymbols;
    }
}
