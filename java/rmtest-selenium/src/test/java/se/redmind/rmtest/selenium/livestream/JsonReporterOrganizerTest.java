package se.redmind.rmtest.selenium.livestream;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.runner.RunWith;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.junit.Cucumber;

import static org.junit.Assert.assertEquals;

/**
 * @author Jeremy Comte
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "json:target/JsonReporterOrganizerTest-json-report.json"}, features = "classpath:se/redmind/rmtest/selenium/livestream/organizer.feature")
public class JsonReporterOrganizerTest {

    public static class Steps {

        private JsonReportOrganizer organizer;

        @Given("^that we parse a json report file \"([^\"]*)\"$")
        public void that_we_parse_a_json_report_file(String file) throws FileNotFoundException {
            JsonElement element = new JsonParser().parse(new FileReader(getClass().getResource(file).getPath()));
            organizer = new JsonReportOrganizer(element.getAsJsonObject());
        }

        @Then("^the report shows that there are (\\d+) tests$")
        public void the_report_shows_that_there_are_tests(int count) {
            assertEquals(count, organizer.getTestCount());
        }

        @Then("^(\\d+) gherkin scenarios$")
        public void gherkin_scenarios(int count) {
            assertEquals(count, organizer.getGherkinScenarios().size());
        }

        @Then("^(\\d+) regular tests$")
        public void regular_tests(int count) {
            assertEquals(count, organizer.getRegularTests().size());
        }

        @Then("^(\\d+) gherkin maps$")
        public void gherkin_maps(int count) {
            assertEquals(count, organizer.getGherkinMap().size());
        }

    }

}
