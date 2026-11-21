package de.learnlib.benchmarking.entity;

import java.time.ZonedDateTime;
import java.util.List;

import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Type;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DBTesterExperiment extends DBExperiment {

    @Lob
    @Setter
    private byte[] learnerState;

    @Enumerated(EnumType.STRING)
    private Learner learner;

    public DBTesterExperiment(Type type,
                              String model,
                              ZonedDateTime timestamp,
                              List<String> alphabet,
                              Learner learner) {
        super(type, model, timestamp, alphabet, new DBStatistics());
        this.learner = learner;
    }

    @Override
    public Kind getKind() {
        return Kind.TESTER;
    }
}
