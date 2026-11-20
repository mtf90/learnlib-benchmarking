package de.learnlib.benchmarking.entity;

import java.time.ZonedDateTime;
import java.util.List;

import de.learnlib.cli.option.Type;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DBLearnerExperiment extends DBExperiment {

    public DBLearnerExperiment(Type type,
                               String model,
                               ZonedDateTime timestamp,
                               List<String> alphabet,
                               String eqoConfig) {
        super(type, model, timestamp, alphabet, new DBStatistics());
        this.eqoConfig = eqoConfig;
    }

    @Lob
    private String eqoConfig;

    @Override
    public Kind getKind() {
        return Kind.LEARNER;
    }
}
