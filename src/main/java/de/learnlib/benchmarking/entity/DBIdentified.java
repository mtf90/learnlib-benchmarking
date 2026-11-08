package de.learnlib.benchmarking.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class DBIdentified {

    @Id
    @GeneratedValue
    private long id;

}
