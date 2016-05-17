package se.redmind.rmtest.cucumber.rest;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.jayway.restassured.response.Response;
import static org.hamcrest.Matchers.*;

import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(glue = "se.redmind.rmtest.cucumber.rest", plugin = "pretty")
public class TestRest {

    private static volatile boolean isServerStarted = false;
    private static SparkServer sparkServer;
	private static int localPort;

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        try {
			sparkServer = new SparkServer().initServices();
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
        localPort = sparkServer.getLocalPort();
    }

    public static class Steps {
    	
    	private RestStep step;

		public Steps(RestStep step) {
			this.step = step;
		}
    	
    	@Given("^custom port is the same as webserver;$")
    	public void custom_port_is_the_same_as_webserver() throws Throwable {
    		step.setPORT(localPort);
    	}
    	
    	@Given("^i set my custom json to:$")
    	public void i_set_my_custom_json_to(String customJson) throws Throwable {
    		step.currentRequest().body(customJson);
    	}
    	
    	@Then("^custom response \"([^\"]*)\" is \"([^\"]*)\"$")
    	public void custom_response_is(String key, String value) throws Throwable {
    		Response res = step.currentResponse();
    		String string = res.body().jsonPath().getString(key);
    		Assert.assertEquals(value, string);
    	}

    	@Then("^custom validatable response \"([^\"]*)\" is \"([^\"]*)\"$")
    	public void custom_validatable_response_is(String query, String value) throws Throwable {
    		step.currentValidatableResponse().body(query, equalTo(value));
    	}
    }

}
