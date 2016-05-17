package se.redmind.rmtest.config;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Assert;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import se.redmind.utils.Fields;

/**
 * @author Jeremy Comte
 */
public class ConfigurationTestSteps {

    private Exception exception;
    private Configuration configuration;

    @When("^we read the following configuration file:$")
    public void we_read_the_following_configuration_file(String config) {
        configuration = Configuration.from(config);
        Assert.assertNotNull(configuration);
    }

    @When("^that we validate it$")
    public void that_we_validate_it() {
        try {
            configuration.validate();
        } catch (Exception e) {
            exception = e;
        }
    }

    @Then("^we get a (.*)")
    public void we_get_a(String exceptionName) {
        Assert.assertNotNull(exception);
        Assert.assertEquals(exception.getClass().getSimpleName(), exceptionName);
    }

    @Then("^we get no error$")
    public void we_get_no_error() {
        Assert.assertNull(exception);
    }

    @Given("^that the system property \"([^\"]*)\" is set to \"([^\"]*)\"$")
    public void that_the_system_property_is_set_to(String property, Object value) {
        System.setProperty(property, String.valueOf(value));
    }

    @When("^that we apply the system properties$")
    public void that_we_apply_the_system_properties() {
        configuration.applySystemProperties();
    }

    @Then("^the configuration property \"([^\"]*)\" is equal to \"([^\"]*)\"")
    public void the_configuration_property_is_equal_to(String property, Object value) throws IllegalArgumentException, IllegalAccessException {
        Map.Entry<Object, Field> entry = Fields.listByPathAndDeclaringInstance(configuration).row(property).entrySet().iterator().next();
        Assert.assertEquals(String.valueOf(value), String.valueOf(entry.getValue().get(entry.getKey())));
    }

    @Then("^the configuration is:$")
    public void the_configuration_is(String value) {
        Assert.assertEquals(Configuration.from(value).toString(), configuration.toString());
    }

}
