package de.learnlib.benchmarking.endpoint;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import de.learnlib.benchmarking.dto.ExperimentDTO;
import de.learnlib.benchmarking.dto.NewExperimentInputDTO;
import de.learnlib.benchmarking.dto.NewExperimentOutputDTO;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.DBExperiment_;
import de.learnlib.benchmarking.entity.DBStatistics;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;
import net.automatalib.exception.FormatException;

/**
 * The endpoint for all experiment related requests.
 */
@Path("experiments")
@Controller
public class ExperimentEndpoint {

    private final EntityManager entityManager;

    /**
     * Default constructor. We depend on Vex's {@link Context} to simulate a request-scoped lifecycle which gives us a
     * fresh entity manager for each request.
     *
     * @param ctx
     *         the context
     */
    public ExperimentEndpoint(Context ctx) {
        this.entityManager = ctx.attribute(EntityManager.class);
    }

    /**
     * Get a list of all known experiments.
     *
     * @return the list of experiments
     */
    @Get
    public List<ExperimentDTO> getAllExperiments() {
        var builder = entityManager.getCriteriaBuilder();
        var q = builder.createQuery(DBExperiment.class);
        var from = q.from(DBExperiment.class);
        var query = entityManager.createQuery(q.select(from).orderBy(builder.asc(from.get(DBExperiment_.timestamp))));

        return query.getResultStream().map(ExperimentDTO::new).toList();
    }

    @Post
    public NewExperimentOutputDTO createNew(NewExperimentInputDTO data) throws IOException, FormatException {

        final var type = data.type();
        final var model = data.model();

        final var loadedModel = type.loadModel(model);
        final var alphabet = new ArrayList<>(loadedModel.alphabet);

        final var experiment = new DBExperiment();
        final var stats = new DBStatistics();

        experiment.setType(type);
        experiment.setModel(model);
        experiment.setTimestamp(ZonedDateTime.now());
        experiment.setAlphabet(alphabet);
        experiment.setStatistics(stats);

        entityManager.persist(stats);
        entityManager.persist(experiment);

        return new NewExperimentOutputDTO(experiment.getId(), alphabet);
    }

}



