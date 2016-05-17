package se.redmind.rmtest.config.testdroid;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

/**
 * @author Jeremy Comte
 */
@RunWith(Cucumber.class)
@CucumberOptions(glue = "se.redmind.rmtest.config",
                 plugin = {"pretty", "html:target/TestDroidConfigurationTest-html-report", "json:target/TestDroidConfigurationTest-json-report.json"})
public class TestDroidConfigurationTest {

}
