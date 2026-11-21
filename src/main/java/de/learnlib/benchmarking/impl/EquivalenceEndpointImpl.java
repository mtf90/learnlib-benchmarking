package de.learnlib.benchmarking.impl;

import java.io.IOException;

import de.learnlib.benchmarking.api.EquivalenceEndpoint;
import de.learnlib.benchmarking.entity.DBExperiment;
import de.learnlib.benchmarking.exception.ExperimentNotFoundException;
import de.learnlib.benchmarking.util.runner.RunnerManager;
import io.avaje.http.api.Controller;
import io.avaje.jex.http.Context;
import jakarta.persistence.EntityManager;
import net.automatalib.exception.FormatException;

@Controller
public class EquivalenceEndpointImpl implements EquivalenceEndpoint {

    private final EntityManager entityManager;
    private final RunnerManager runnerManager;

    public EquivalenceEndpointImpl(Context ctx, RunnerManager runnerManager) {
        this.entityManager = ctx.attribute(EntityManager.class);
        this.runnerManager = runnerManager;
    }

    @Override
    public boolean testEquivalence(long id, String hypothesis) throws IOException, FormatException {
        var experiment = this.entityManager.find(DBExperiment.class, id);

        if (experiment == null) {
            throw new ExperimentNotFoundException();
        }

        return runnerManager.testEquivalence(experiment, hypothesis);
    }
}
