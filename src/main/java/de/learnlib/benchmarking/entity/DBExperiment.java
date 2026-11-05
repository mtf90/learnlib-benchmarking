package de.learnlib.benchmarking.entity;

import io.avaje.jsonb.Json;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

@Entity
@Json
public class DBExperiment extends DBIdentified {

    @OneToOne
    private DBStatistics statistics;

    public DBStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(DBStatistics statistics) {
        this.statistics = statistics;
    }
}
