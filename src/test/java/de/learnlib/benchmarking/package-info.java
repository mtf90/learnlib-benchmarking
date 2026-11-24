@Client.Import({EquivalenceEndpoint.class,
                ExperimentEndpoint.class,
                LearnerEndpointDFA.class,
                LearnerEndpointMealy.class,
                StatisticsEndpoint.class,
                TesterEndpointDFA.class,
                TesterEndpointMealy.class})
package de.learnlib.benchmarking;

import de.learnlib.benchmarking.api.EquivalenceEndpoint;
import de.learnlib.benchmarking.api.ExperimentEndpoint;
import de.learnlib.benchmarking.api.StatisticsEndpoint;
import de.learnlib.benchmarking.api.LearnerEndpointDFA;
import de.learnlib.benchmarking.api.LearnerEndpointMealy;
import de.learnlib.benchmarking.api.TesterEndpointDFA;
import de.learnlib.benchmarking.api.TesterEndpointMealy;
import io.avaje.http.api.Client;