package de.learnlib.benchmarking;

import io.avaje.http.client.HttpClient;
import io.avaje.jex.Jex.Server;
import org.kohsuke.MetaInfServices;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestNGListener;

@MetaInfServices(ITestNGListener.class)
public class ServerLifecycleListener implements ISuiteListener {

    private static Server server;

    private static HttpClient client;

    @Override
    public void onStart(ISuite suite) {
        server = Main.buildJex().port(0).start();
        client = HttpClient.builder().baseUrl("http://localhost:" + server.port()).build();
    }

    @Override
    public void onFinish(ISuite suite) {
        if (server != null) {
            server.shutdown();
        }
    }

    public static HttpClient getClient() {
        return client;
    }
}
