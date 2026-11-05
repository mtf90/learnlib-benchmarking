package de.learnlib.benchmarking.endpoint;

import java.io.IOException;
import java.io.InputStream;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.MediaType;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;

@Path("openapi")
@Controller
public class OpenAPIEndpoint {

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public String json() {
        try (InputStream is = OpenAPIEndpoint.class.getResourceAsStream("/meta/openapi.json")) {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}



