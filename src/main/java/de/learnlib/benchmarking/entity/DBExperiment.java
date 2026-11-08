package de.learnlib.benchmarking.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DBExperiment extends DBIdentified {

    @Enumerated(EnumType.STRING)
    private ModelType type;

    private String model;

    private ZonedDateTime timestamp;

    private int alphabetSize;

    @OneToOne
    private DBStatistics statistics;

}
