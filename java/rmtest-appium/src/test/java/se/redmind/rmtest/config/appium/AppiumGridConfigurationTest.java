package se.redmind.rmtest.config.appium;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

/**
 * @author Jeremy Comte
 */
@RunWith(Cucumber.class)
@CucumberOptions(glue = "se.redmind.rmtest.config",
                 plugin = {"pretty", "html:target/AppiumGridConfigurationTest-html-report", "json:target/AppiumGridConfigurationTest-json-report.json"})
public class AppiumGridConfigurationTest {

}
