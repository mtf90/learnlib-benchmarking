package de.learnlib.benchmarking.entity;

import io.avaje.jsonb.Json;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class DBIdentified {

    @Id
    @GeneratedValue
    @Json.Ignore(serialize = true)
    private long id;

    public long getId() {
        return id;
    }
}
