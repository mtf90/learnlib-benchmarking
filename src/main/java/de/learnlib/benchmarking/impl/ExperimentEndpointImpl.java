package de.learnlib.benchmarking.impl;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import de.learnlib.benchmarking.api.ExperimentEndpoint;
import de.learnlib.benchmarking.dto.EQOConfig;
import de.learnlib.benchmarking.dto.ExperimentDTO;
import de.learnlib.benchmarking.dto.NewExperimentInputDTO;
import de.learnlib.benchmarking.dto.NewExperimentOutputDTO;
import de.learnlib.benchmarking.dto.NewTesterExperimentInputDTO;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.entity.DBExperiment_;
import de.learnlib.benchmarking.entity.DBStatistics;
import de.learnlib.benchmarking.util.cache.AbstractModelCache;
import de.learnlib.benchmarking.util.runner.RunnerManager;
import io.avaje.http.api.Controller;
import io.avaje.jex.http.Context;
import io.avaje.jsonb.JsonType;
import jakarta.persistence.EntityManager;
import net.automatalib.exception.FormatException;

/**
 * The endpoint for all experiment related requests.
 */
@Controller
public class ExperimentEndpointImpl implements ExperimentEndpoint {

    private final EntityManager entityManager;
    private final JsonType<EQOConfig> eqoSerializer;
    private final RunnerManager runnerManager;

    /**
     * Default constructor. We depend on Vex's {@link Context} to simulate a request-scoped lifecycle which gives us a
     * fresh entity manager for each request.
     *
     * @param ctx
     *         the context
     */
    public ExperimentEndpointImpl(Context ctx, JsonType<EQOConfig> eqoSerializer, RunnerManager runnerManager) {
        this.entityManager = ctx.attribute(EntityManager.class);
        this.eqoSerializer = eqoSerializer;
        this.runnerManager = runnerManager;
    }

    @Override
    public List<ExperimentDTO> getAllExperiments() {
        var builder = entityManager.getCriteriaBuilder();
        var q = builder.createQuery(DBExperiment.class);
        var from = q.from(DBExperiment.class);
        var query = entityManager.createQuery(q.select(from).orderBy(builder.asc(from.get(DBExperiment_.timestamp))));

        return query.getResultStream().map(ExperimentDTO::new).toList();
    }

    @Override
    public NewExperimentOutputDTO createForLearner(NewExperimentInputDTO data) throws IOException, FormatException {

        final var type = data.type();
        final var model = data.model();
        final var paramsAsString = eqoSerializer.toJson(data.eqoConfig());

        final var loadedModel = AbstractModelCache.loadModel(type, model);
        final var alphabet = new ArrayList<>(loadedModel.alphabet);

        final var experiment =
                new DBExperiment(type, model, ZonedDateTime.now(), alphabet, paramsAsString, new DBStatistics());

        entityManager.persist(experiment);

        return new NewExperimentOutputDTO(experiment.getId(), alphabet);
    }

    @Override
    public NewExperimentOutputDTO createForTester(NewTesterExperimentInputDTO data)
            throws IOException, FormatException {

        final var type = data.type();
        final var model = data.model();

        final var loadedModel = AbstractModelCache.loadModel(type, model);
        final var alphabet = new ArrayList<>(loadedModel.alphabet);

        final var experiment = new DBExperiment(type, model, ZonedDateTime.now(), alphabet, "", new DBStatistics());

        entityManager.persist(experiment);
        entityManager.flush();

        var runner = runnerManager.createAndRegisterRunner(experiment,
                                                           entityManager.getEntityManagerFactory(),
                                                           data.learner(),
                                                           type);

        runner.start();

        return new NewExperimentOutputDTO(experiment.getId(), alphabet);
    }
}



