package de.learnlib.benchmarking.api;

import java.io.IOException;

import io.avaje.http.api.Body;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.automatalib.exception.FormatException;

@Path("equivalence")
public interface EquivalenceEndpoint {

    @Post("/{id}")
    @Tag(name = "misc")
    boolean testEquivalence(long id, @Body String hypothesis) throws IOException, FormatException;

}
