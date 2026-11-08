package de.learnlib.benchmarking.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DBStatistics extends DBIdentified {

    private int numberOfLearnerQueries;

    private int numberOfLearnerSymbols;

    private int numberOfTesterQueries;

    private int numberOfTesterSymbols;

}
