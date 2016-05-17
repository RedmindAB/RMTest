package se.redmind.rmtest.cucumber.rest;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.math.NumberUtils;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class RestStep {

	private int PORT = 0;
    private ValidatableResponse vResponse;
    private Response response;
    private RequestSpecification requestSpecification;

    public RestStep() {
        requestSpecification = given();
    }

    @Given("^that url is \"([^\"]*)\"$")
    public void that_url_is(String url) throws Throwable {
        requestSpecification.baseUri(url);
    }

    @Given("^port is (\\d+)$")
    public void port_is(int port) throws Throwable {
    	setPORT(port);
    }

    @When("^we (get|post|put|update|patch|delete) \"([^\"]*)\"$")
    public void we_get(String action, String path) throws Throwable {
    	requestSpecification.port(getPORT());
    	switch (action) {
		case "get":
			response = requestSpecification.get(path);
			break;
		case "post":
			response = requestSpecification.post(path);
			break;
		case "put":
			response = requestSpecification.put(path);
		case "update":
		case "patch":
			response = requestSpecification.patch(path);
			break;
		case "delete":
			response = requestSpecification.delete(path);
			break;
		default:
			break;
		}
        this.vResponse = response.then();
    }

    @Given("^we send param \"([^\"]*)\" with ?(?:value)? \"([^\"]*)\"$")
    public void we_send_param_with_value(String key, String value) throws Throwable {
        requestSpecification.param(key, value);
    }

    @Given("^we send:$")
    public void we_send(String content) throws Throwable {
        requestSpecification = requestSpecification.body(content);
    }

    @Then("^status is (\\d+)$")
    public void status_is(int status) throws Throwable {
        vResponse.assertThat().statusCode(status);
    }

    @Then("^content is \"([^\"]*)\"$")
    public void content_is(String contentContains) throws Throwable {
        String body = response.body().asString();
        assertEquals(contentContains, body);
    }

    @Then("^json key \"([^\"]*)\" is \"([^\"]*)\"$")
    public void json_key_is(String key, String value) throws Throwable {
        vResponse.body(key, is(value));
    }

    @Then("^header \"([^\"]*)\" is \"([^\"]*)\"$")
    public void header_is(String headerKey, String headerValue) throws Throwable {
        vResponse.header(headerKey, headerValue);
    }
    
    @Given("^we set header \"([^\"]*)\" to \"([^\"]*)\"$")
    public void we_set_header_to(String headerName, String headerValue) throws Throwable {
    	requestSpecification.header(headerName, headerValue);
    }
    
    @Then("^index (\\d+) has the key \"([^\"]*)\" and value \"([^\"]*)\"$")
    public void index_has_the_key_and_value(int index, String key, String value) throws Throwable {
        vResponse.body("[" + index + "]." + key, is(value));
    }

    @Then("^parameter \"([^\"]*)\" (?:is?( not)?) \"?([^\"]*|\\d+.\\d+|\\d+)?\"?$")
    public void object_has_the_value(String query, Boolean not, String value) throws Throwable {
        not = not == null ? false : not;
        if (NumberUtils.isNumber(value)) {
            if (value.contains(".")) {
                vResponse.body(query, equalTo(Float.valueOf(value)));
            } else {
                vResponse.body(query, equalTo(Integer.valueOf(value)));
            }
        } else {
        	vResponse.body(query, equalTo(value));
        }
    }
    
    @Then("^parameter \"([^\"]*)\" has no value$")
    public void parameter_has_no_value(String query) throws Throwable {
    	String pathParameter = response.path(query);
    	if(pathParameter == null){
    		assertNull(pathParameter);
    	}
    	else if(pathParameter.isEmpty()){
    		assertTrue(pathParameter.isEmpty());
    	}
    	else{
    		fail("The object has a value: '"+pathParameter+"'");
    	}
    }

    @Then("^size of \"([^\"]*)\" is (\\d+)$")
    public void array_of_size_is(String key, int size) throws Throwable {
        vResponse.body(key, hasSize(size));
    }

    @Then("^time is below (\\d+) ?(milliseconds|seconds)?$")
    public void time_is_below_milliseconds(int maxTime, TimeUnit timeUnit) throws Throwable {
        if (timeUnit == null) {
            timeUnit = TimeUnit.MILLISECONDS;
        }
        long time = response.timeIn(timeUnit);
        assertTrue("the request took " + time + " " + timeUnit + " and the timeout was " + maxTime, time <= maxTime);
    }
    
    @Given("^we set cookie \"([^\"]*)\" to \"([^\"]*)\"$")
    public void we_set_cookie_to(String cookieKey, String cookieValue) throws Throwable {
    	this.requestSpecification.cookie(cookieKey, cookieValue);
    }

	public RequestSpecification currentRequest() {
		return this.requestSpecification;
	}
	
	public Response currentResponse(){
		return this.response;
	}
	
	public ValidatableResponse currentValidatableResponse(){
		return this.vResponse;
	}

	public int getPORT() {
		return PORT;
	}

	public void setPORT(int pORT) {
		PORT = pORT;
	}

}
