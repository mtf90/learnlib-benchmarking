package de.learnlib.benchmarking.entity;

import java.time.ZonedDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DBExperiment extends DBIdentified {

    @Enumerated(EnumType.STRING)
    private ModelType type;

    private String model;

    private ZonedDateTime timestamp;

    private List<String> alphabet;

    @Lob
    private String eqoConfig;

    @OneToOne
    private DBStatistics statistics;

}
