package de.learnlib.benchmarking.endpoint;

import java.util.Collections;
import java.util.List;

import de.learnlib.benchmarking.dto.QueryDTO;
import de.learnlib.benchmarking.entity.DBExperiment;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;

@Controller
@Path("/learner")
public class LearnerEndpoint {

    private final EntityManager entityManager;

    /**
     * Default constructor. We depend on Vex's {@link Context} to simulate a request-scoped lifecycle which gives us a
     * fresh entity manager for each request.
     *
     * @param ctx the context
     */
    public LearnerEndpoint(Context ctx) {
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    @Post("/queries/{id}")
    public List<QueryDTO> answerQueries(long id, List<QueryDTO> queries) {
        final var experiment = entityManager.find(DBExperiment.class, id);
        final var statistics = experiment.getStatistics();

        final int oldQueries = statistics.getNumberOfQueries();
        final int oldSymbols = statistics.getNumberOfSymbols();

        final int newQueries = queries.size();
        int newSymbols = 0;

        for (QueryDTO query : queries) {
            newSymbols += query.symbols().size();
        }

        statistics.setNumberOfQueries(oldQueries + newQueries);
        statistics.setNumberOfSymbols(oldSymbols + newSymbols);

        return Collections.emptyList();
    }

}
