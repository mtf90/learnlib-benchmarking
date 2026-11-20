package de.learnlib.benchmarking.entity;

import java.time.ZonedDateTime;
import java.util.List;

import de.learnlib.cli.option.Type;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DBRunnerExperiment extends DBExperiment {

    @Enumerated(EnumType.STRING)
    private Status status;

    public DBRunnerExperiment(Type type, String model, ZonedDateTime timestamp, List<String> alphabet, Status status) {
        super(type, model, timestamp, alphabet, new DBStatistics());
        this.status = status;
    }

    @Override
    public Kind getKind() {
        return Kind.RUNNER;
    }

    public enum Status {
        RUNNING,
        FINISHED
    }
}
