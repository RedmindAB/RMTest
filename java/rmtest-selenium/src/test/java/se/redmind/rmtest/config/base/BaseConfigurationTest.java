package se.redmind.rmtest.config.base;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

/**
 * @author Jeremy Comte
 */
@RunWith(Cucumber.class)
@CucumberOptions(glue = "se.redmind.rmtest.config",
                 plugin = {"pretty", "html:target/BaseConfigurationTest-html-report", "json:target/BaseConfigurationTest-json-report.json"})
public class BaseConfigurationTest {

}
