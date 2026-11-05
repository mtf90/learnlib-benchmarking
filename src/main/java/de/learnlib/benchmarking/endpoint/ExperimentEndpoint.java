package de.learnlib.benchmarking.endpoint;

import java.util.List;

import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.DBStatistics;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;

@Path("experiments")
@Controller
public class ExperimentEndpoint {

    private final EntityManager entityManager;

    /**
     * Default constructor. We depend on Vex's {@link Context} to simulate a request-scoped lifecycle which gives us a
     * fresh entity manager for each request.
     *
     * @param ctx the context
     */
    public ExperimentEndpoint(Context ctx) {
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    @Get
    public List<DBExperiment> getAllExperiments() {
        var q = entityManager.getCriteriaBuilder().createQuery(DBExperiment.class);
        var query = entityManager.createQuery(q.select(q.from(DBExperiment.class)));

        return query.getResultList();
    }

    @Post
    public long createNew() {
        var experiment = new DBExperiment();
        var stats = new DBStatistics();

        experiment.setStatistics(stats);

        entityManager.persist(stats);
        entityManager.persist(experiment);

        return experiment.getId();
    }

}



