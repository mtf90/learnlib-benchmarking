package de.learnlib.benchmarking.endpoint;

import de.learnlib.benchmarking.BenchmarkingModule;
import io.avaje.http.api.Controller;

/**
 * We need one controller without any dependencies to enable the generation of our {@link BenchmarkingModule} which
 * kicks of avaje CDI.
 */
@Controller
public class Activation {}
