package de.learnlib.benchmarking.entity;

import java.time.ZonedDateTime;
import java.util.List;

import de.learnlib.cli.option.Type;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DBTesterExperiment extends DBExperiment {

    @Lob
    private String finalHypothesis;

    public DBTesterExperiment(Type type,
                              String model,
                              ZonedDateTime timestamp,
                              List<String> alphabet,
                              String finalHypothesis) {
        super(type, model, timestamp, alphabet, new DBStatistics());
        this.finalHypothesis = finalHypothesis;
    }

    @Override
    public Kind getKind() {
        return Kind.TESTER;
    }
}
