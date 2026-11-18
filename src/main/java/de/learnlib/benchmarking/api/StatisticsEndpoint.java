package de.learnlib.benchmarking.api;

import de.learnlib.benchmarking.dto.StatisticsDTO;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("statistics")
public interface StatisticsEndpoint {

    @Get("{id}")
    @Tag(name = "misc")
    StatisticsDTO getStatistics(long id);
}
