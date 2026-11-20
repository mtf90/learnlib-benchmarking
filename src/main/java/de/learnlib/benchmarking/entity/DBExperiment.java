package de.learnlib.benchmarking.entity;

import java.time.ZonedDateTime;
import java.util.List;

import de.learnlib.cli.option.Type;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DBExperiment extends DBIdentified {

    @Enumerated(EnumType.STRING)
    private Type type;

    private String model;

    private ZonedDateTime timestamp;

    private List<String> alphabet;

    @OneToOne(cascade = CascadeType.ALL)
    private DBStatistics statistics;

    public abstract Kind getKind();

    public enum Kind {
        TESTER,
        LEARNER,
        RUNNER
    }

}
