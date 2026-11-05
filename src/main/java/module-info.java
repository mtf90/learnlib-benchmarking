import de.learnlib.benchmarking.endpoint.EndpointModule;
import de.learnlib.benchmarking.jsonb.GeneratedJsonComponent;
import io.avaje.inject.spi.InjectExtension;
import io.avaje.jsonb.spi.JsonbExtension;

open module de.learnlib.benchmarking {

    requires java.compiler;

    requires io.avaje.http.api;
    requires io.avaje.inject;
    requires io.avaje.jex;
    requires io.avaje.jsonb;
    requires io.avaje.jsonb.plugin;

    requires jakarta.persistence;
    requires jakarta.inject;

    requires org.eclipse.persistence.jpa;
    requires org.objectweb.asm;

    requires org.hsqldb;

    uses jakarta.persistence.spi.PersistenceProvider;

    provides InjectExtension with EndpointModule;
    provides JsonbExtension with GeneratedJsonComponent;
}
