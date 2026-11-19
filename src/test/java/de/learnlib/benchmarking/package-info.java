@Client.Import({ExperimentEndpoint.class,
                LearnerEndpointDFA.class,
                LearnerEndpointMealy.class,
                StatisticsEndpoint.class,
                TesterEndpointDFA.class,
                TesterEndpointMealy.class})
package de.learnlib.benchmarking;

import de.learnlib.benchmarking.api.ExperimentEndpoint;
import de.learnlib.benchmarking.api.StatisticsEndpoint;
import de.learnlib.benchmarking.api.learner.LearnerEndpointDFA;
import de.learnlib.benchmarking.api.learner.LearnerEndpointMealy;
import de.learnlib.benchmarking.api.tester.TesterEndpointDFA;
import de.learnlib.benchmarking.api.tester.TesterEndpointMealy;
import io.avaje.http.api.Client;