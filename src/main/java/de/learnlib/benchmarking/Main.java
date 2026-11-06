package de.learnlib.benchmarking;

import java.util.List;

import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import io.avaje.jex.http.Context;
import io.avaje.jex.staticcontent.StaticContent;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@OpenAPIDefinition(info = @Info(title = "LearnLib Benchmarking",
                                description = "A microservice for benchmarking active automata learning algorithms."))
public class Main {

    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("pu");

    public static void main(String[] args) {

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

        Jex.create()
           .plugin(swaggerResources)
           .plugin(openAPIResource)
           .routing(services)
           .routing(r -> r.before(Main::preHook))
           .routing(r -> r.after(Main::postHook))
           .start();
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
