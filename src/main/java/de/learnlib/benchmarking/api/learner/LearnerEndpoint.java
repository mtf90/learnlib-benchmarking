package de.learnlib.benchmarking.api.learner;

import java.io.IOException;
import java.util.List;

import io.avaje.http.api.Body;
import io.avaje.http.api.Consumes;
import io.avaje.http.api.MediaType;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.automatalib.exception.FormatException;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface LearnerEndpoint<MQOUT, CEOUT> {

    /**
     * Submit queries to be answered by the respective system under learning.
     *
     * @param id
     *         the experiment id
     * @param queries
     *         the list of queries to answer
     *
     * @return the (ordered) list of responses to the queries
     */
    @Post("/queries/{id}")
    @Produces(statusCode = 200)
    @Tag(name = "learner")
    MQOUT answerQueries(long id, List<List<String>> queries);

    /**
     * Submit a (tentative) hypothesis model to let the experiment runner search for counterexamples. This call blocks
     * until the equivalence oracle specified by your experiment configurations has reached a conclusion.
     *
     * @param id
     *         the experiment id
     * @param hypothesis
     *         the hypothesis (encoded in the .dot format)
     *
     * @return a counterexample to the provided hypothesis or {@code null} if no such counterexample can be found
     *
     * @throws IOException
     *         in case parsing fails
     * @throws FormatException
     *         in case parsing fails
     */
    @Post("/counterexample/{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(statusCode = 200)
    @Tag(name = "learner")
    @Nullable
    CEOUT findCounterExample(long id, @Body String hypothesis) throws IOException, FormatException;
}
