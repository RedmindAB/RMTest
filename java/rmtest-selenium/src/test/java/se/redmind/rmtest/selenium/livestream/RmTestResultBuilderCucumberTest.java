package se.redmind.rmtest.selenium.livestream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.junit.Cucumber;
import gherkin.formatter.model.Scenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Victor Mattsson on 2016-02-10.
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "json:target/RmTestResultBuilderCucumberTest-json-report.json"}, features = "classpath:se/redmind/rmtest/selenium/livestream/builder.feature")
public class RmTestResultBuilderCucumberTest {

    public static class Steps {

        private RmTestResultBuilder resultBuilder = new RmTestResultBuilder();
        private JsonObject properties;
        private Set<Map.Entry<String, JsonElement>> reportPropertiesEntries;
        private String propertyName;
        private String propertyValue;
        private String[] displayNames;
        private JsonObject report;
        private JsonArray tests;
        private JsonObject testResult;
        private Description descriptionScenario;
        private Description ordinaryDescription;

        @Given("^that we apply the system property \"([^\"]*)\" to \"([^\"]*)\"$")
        public void that_we_apply_the_system_property_to(String name, String value) {
            propertyName = name;
            propertyValue = value;
            System.setProperty(propertyName, propertyValue);
        }

        @Given("^build a json object containing the system properties$")
        public void build_a_json_object_containing_the_system_properties() {
            JsonObject build = resultBuilder.build();
            properties = build.get("properties").getAsJsonObject();
        }

        @Given("^converts it to a list$")
        public void converts_it_to_a_list() {
            reportPropertiesEntries = properties.entrySet();
        }

        @Then("^the list should be the same size as system properties$")
        public void the_list_should_be_the_same_size_as_system_properties() {
            assertEquals(System.getProperties().size(), reportPropertiesEntries.size());
        }

        @Then("^the custom property should be the same as \"([^\"]*)\"$")
        public void the_custom_property_should_be_the_same_as(String arg1) throws Throwable {
            String customProperty = properties.get(propertyName).getAsString();
            assertEquals(customProperty, propertyValue);
        }

        @Given("^that we have a list of tests with display names:$")
        public void that_we_have_three_tests_tests_with_display_names(List<String> names) {
            displayNames = new String[names.size()];
            for (int i = 0; i < displayNames.length; i++) {
                displayNames[i] = names.get(i);
            }
        }

        @Given("^we add tests to result builder$")
        public void we_add_tests_to_result_builder() {
            for (int i = 0; i < displayNames.length; i++) {
                resultBuilder.addTest(displayNames[i], Description.createSuiteDescription("testMethod" + i + 1
                    + "(se.redmind.rmtest.LiveStream)"));
            }
        }

        @Given("^build the JsonObject$")
        public void build_the_JsonObject() {
            report = resultBuilder.build();
            tests = report.get("tests").getAsJsonArray();
        }

        @Given("^add test (\\d+) as \"([^\"]*)\"$")
        public void add_test_as(int test, String resultType) throws Throwable {
            switch (resultType) {
                case "finished":
                    resultBuilder.addFinishedTest(displayNames[test - 1]);
                    break;
                case "failure":
                    resultBuilder.addTestFailure(displayNames[test - 1],
                        new Failure(Description.createSuiteDescription(this.getClass()), new NullPointerException()));
                    break;
                case "ignored":
                    resultBuilder.addIgnoredTest(displayNames[test - 1]);
                    break;
                case "assumptionfailure":
                    resultBuilder.addAssumptionFailure(displayNames[test - 1],
                        new Failure(Description.createSuiteDescription(this.getClass()), new NullPointerException()));
                    break;
                default:
                    System.out.println("Result typ invalid. valid types: finished, failure, assumptionfailure and ignored");
            }
        }

        @Then("^the total number of tests should be (\\d+)$")
        public void the_total_number_of_tests_should_be(int testSize) {
            int totalTests = report.get("totalTests").getAsInt();
            assertEquals(testSize, totalTests);
            assertEquals(testSize, tests.size());
        }

        @Then("^the (\\d+)(?:st|nd|rd|th) test should be \"([^\"]*)\"$")
        public void the_first_tests_should_be_(int number, String wantedResult) {
            String actualResult = getTestResult(number);
            assertEquals(number, getResultId());
            assertEquals(wantedResult, actualResult);
        }

        @Given("^we create a Description that is a Scenario$")
        public void we_create_a_Description_that_is_a_Scenario() {
            descriptionScenario = Description.createTestDescription(
                "se.redmind.rmtest.LiveStream",
                "testMethod",
                new Scenario(new ArrayList<>(), new ArrayList<>(), "Scenario: ", "This is a scenario", "", 1, "1")
            );
        }

        @Then("^the test is a gherkin$")
        public void the_test_is_a_gherkin() throws Throwable {
            assertTrue(resultBuilder.isGherkin(descriptionScenario));
        }

        @Given("^we create a Description that is not a Scenario$")
        public void we_create_a_Description_that_is_not_a_Scenario() throws Throwable {
            ordinaryDescription = Description.createSuiteDescription("testMethod(se.redmind.rmtest.LiveStream)");
        }

        @Then("^the test is not a gherkin$")
        public void the_test_is_not_a_gherkin() throws Throwable {
            assertFalse(resultBuilder.isGherkin(ordinaryDescription));
        }

        private int getResultId() {
            return testResult.get("id").getAsInt();
        }

        private String getTestResult(int index) {
            testResult = tests.get(index - 1).getAsJsonObject();
            return testResult.get("result").getAsString();
        }
    }
}
