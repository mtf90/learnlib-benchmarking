package de.learnlib.benchmarking;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.learnlib.benchmarking.util.RequestLogger;
import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import io.avaje.jex.http.Context;
import io.avaje.jex.staticcontent.StaticContent;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@OpenAPIDefinition(info = @Info(title = "LearnLib Benchmarking",
                                description = "A microservice for benchmarking active automata learning algorithms."),
                   tags = {@Tag(name = "misc",
                                description = "Miscellaneous endpoints for creating experiments, accessing statistics, etc."),
                           @Tag(name = "learner", description = "Endpoints for benchmarking your learner"),
                           @Tag(name = "tester", description = "Endpoints for benchmarking your tester")})
public class Main {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("pu", System.getProperties());

    static void main() throws IOException {
        var dbDirectory = Path.of("data");
        if (!Files.exists(dbDirectory)) {
            Files.createDirectory(dbDirectory);
        }
        final var jex = buildJex();
        jex.start();
    }

    public static Jex buildJex() {
        final var swaggerResources = StaticContent.ofClassPath("/swagger/")
                                                  .route("/")
                                                  .directoryIndex("index.html")
                                                  .resourceLoader(Main.class)
                                                  .build();
        final var openAPIResource = StaticContent.ofClassPath("/meta/openapi.json")
                                                 .route("/openapi.json")
                                                 .resourceLoader(Main.class)
                                                 .build();

        final List<Routing.HttpService> services;
        try (BeanScope beanScope = BeanScope.builder().build()) {
            services = beanScope.list(Routing.HttpService.class);
        }

        return Jex.create()
                  .plugin(swaggerResources)
                  .plugin(openAPIResource)
                  .routing(services)
                  .routing(r -> r.before(Main::preHook))
                  .routing(r -> r.after(Main::postHook))
                  .config(cfg -> cfg.maxRequestSize(-1))
                  .filter(new RequestLogger());
    }

    public static void preHook(Context ctx) {
        EntityManager entityManager = EMF.createEntityManager();
        entityManager.getTransaction().begin();
        ctx.attribute(EntityManager.class, entityManager);
    }

    public static void postHook(Context ctx) {
        EntityManager entityManager = ctx.attribute(EntityManager.class);
        try {
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }
}
