package de.learnlib.benchmarking.util;

import de.learnlib.benchmarking.dto.EQOConfig;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import jakarta.inject.Singleton;

@Factory
public class JsonConfig {

    @Bean
    @Singleton
    public Jsonb jsonb() {
        return Jsonb.builder().build();
    }

    @Bean
    @Singleton
    public JsonType<EQOConfig> eqoParamsParser(Jsonb jsonb) {
        return jsonb.type(EQOConfig.class);
    }
}
