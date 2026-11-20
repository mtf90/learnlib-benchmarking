package de.learnlib.benchmarking.impl;

import de.learnlib.benchmarking.api.StatisticsEndpoint;
import de.learnlib.benchmarking.dto.StatisticsDTO;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.DBRunnerExperiment;
import de.learnlib.benchmarking.entity.DBRunnerExperiment.Status;
import de.learnlib.benchmarking.exception.ExperimentNotYetFinishedException;
import io.avaje.http.api.Controller;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;

@Controller
public class StatisticsEndpointImpl implements StatisticsEndpoint {

    private final EntityManager entityManager;

    /**
     * Default constructor. We depend on Jex's {@link Context} to simulate a request-scoped lifecycle which gives us a
     * fresh entity manager for each request.
     *
     * @param ctx
     *         the context
     */
    public StatisticsEndpointImpl(Context ctx) {
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    @Override
    public StatisticsDTO getStatistics(long id) {
        var q = entityManager.find(DBExperiment.class, id);

        if (q instanceof DBRunnerExperiment exp && exp.getStatus() == Status.RUNNING) {
            throw new ExperimentNotYetFinishedException();
        }

        return new StatisticsDTO(q.getStatistics());
    }
}
