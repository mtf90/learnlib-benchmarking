package de.learnlib.benchmarking;

import java.util.List;

import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Main {

    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("pu");

    public static void main(String[] args) {
        final List<Routing.HttpService> services;

        try (BeanScope beanScope = BeanScope.builder().build()) {
            services = beanScope.list(Routing.HttpService.class);
        }

        Jex.create()
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
