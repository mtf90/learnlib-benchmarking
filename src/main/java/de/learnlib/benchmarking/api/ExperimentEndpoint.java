package de.learnlib.benchmarking.api;

import java.io.IOException;
import java.util.List;

import de.learnlib.benchmarking.dto.ExperimentDTO;
import de.learnlib.benchmarking.dto.NewExperimentInputDTO;
import de.learnlib.benchmarking.dto.NewExperimentOutputDTO;
import de.learnlib.benchmarking.dto.NewRunnerExperimentInputDTO;
import de.learnlib.benchmarking.dto.NewTesterExperimentInputDTO;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.automatalib.exception.FormatException;

/**
 * The endpoint for all experiment related requests.
 */
@Path("experiments")
public interface ExperimentEndpoint {

    /**
     * Get a list of all known experiments.
     *
     * @return the list of experiments
     */
    @Get
    @Tag(name = "misc")
    List<ExperimentDTO> getAllExperiments();

    @Post("learner")
    @Tag(name = "misc")
    NewExperimentOutputDTO createForLearner(NewExperimentInputDTO data) throws IOException, FormatException;

    @Post("tester")
    @Tag(name = "misc")
    NewExperimentOutputDTO createForTester(NewTesterExperimentInputDTO data) throws IOException, FormatException;

    @Post("runner")
    @Tag(name = "misc")
    NewExperimentOutputDTO createForRunner(NewRunnerExperimentInputDTO data) throws IOException, FormatException;
}
