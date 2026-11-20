package de.learnlib.benchmarking.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class DBIdentified {

    @Id
    @GeneratedValue
    private long id;

    @Version
    private int version;

}
