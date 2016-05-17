package se.redmind.rmtest.cucumber.web;

import cucumber.api.PendingException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import se.redmind.rmtest.runners.ParameterizedCucumberRunnerFactory;
import se.redmind.rmtest.runners.WebDriverRunner;
import se.redmind.rmtest.runners.WebDriverRunnerOptions;
import se.redmind.utils.Fields;
import spark.Spark;
import spark.webserver.JettySparkServer;

import static spark.Spark.get;

/**
 * @author Jeremy Comte
 */
@RunWith(WebDriverRunner.class)
@WebDriverRunnerOptions(reuseDriver = true)
@Parameterized.UseParametersRunnerFactory(ParameterizedCucumberRunnerFactory.class)
@CucumberOptions(plugin = {"pretty", "json:target/WebDriverStepsTest-json-report.json"}, strict = true)
public class WebDriverStepsTest {

    private static boolean isServerRunning;
    private static int localPort;

    @BeforeClass
    public static synchronized void createTestServer() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (!isServerRunning) {
            isServerRunning = true;
            Spark.staticFileLocation("/html");
            Spark.port(0);
            get("/", (request, response) -> {
                return "hello!";
            });
            get("/cookie/valueOf/:name", (request, response) -> {
                response.type("text/plain");
                return String.valueOf(request.cookie(request.params("name")));
            });
            Spark.awaitInitialization();
            JettySparkServer sparkServer = Fields.getValue(Spark.getInstance(), "server");
            Server jettyServer = Fields.getValue(sparkServer, "server");
            ServerConnector connector = (ServerConnector) jettyServer.getConnectors()[0];
            localPort = connector.getLocalPort();
        }
    }


    public static class Steps {

        private final WebDriverSteps driverSteps;

        public Steps(WebDriverSteps driverSteps) {
            this.driverSteps = driverSteps;
        }

        @Given("^that we know our local spark instance$")
        public void that_we_know_our_local_spark_instance() {
            driverSteps.that_we_know_the_element_named_as(null, null, "http://localhost:" + localPort, "spark");
        }

        @Given("^that we know the current path as \"([^\"]*)\"$")
        public void thatWeKnowTheCurrentPathAs(String name) {
            driverSteps.that_we_know_the_element_named_as(null, null, System.getProperty("user.dir"), name);
        }
    }

}
