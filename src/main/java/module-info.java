import de.learnlib.benchmarking.BenchmarkingModule;
import de.learnlib.benchmarking.dto.jsonb.GeneratedJsonComponent;
import io.avaje.inject.spi.InjectExtension;
import io.avaje.jsonb.spi.JsonbExtension;

open module de.learnlib.benchmarking {

    requires java.compiler;

    requires de.learnlib.api;
    requires de.learnlib.oracle.equivalence;

    requires net.automatalib.api;
    requires net.automatalib.core;
    requires net.automatalib.serialization.dot;

    requires org.slf4j;

    requires io.avaje.http.api;
    requires io.avaje.inject;
    requires io.avaje.jex;
    requires io.avaje.jex.staticcontent;
    requires io.avaje.jsonb;
    requires io.avaje.jsonb.plugin;

    requires jakarta.persistence;
    requires jakarta.inject;

    requires org.eclipse.persistence.jpa;
    requires org.eclipse.persistence.jpa.jpql;
    requires org.objectweb.asm;

    requires org.hsqldb;

    requires io.swagger.v3.oas.annotations;

    requires static lombok;

    uses jakarta.persistence.spi.PersistenceProvider;

    provides InjectExtension with BenchmarkingModule;
    provides JsonbExtension with GeneratedJsonComponent;
}
