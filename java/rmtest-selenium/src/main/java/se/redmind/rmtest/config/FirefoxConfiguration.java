package se.redmind.rmtest.config;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Jeremy Comte
 */
@JsonTypeName("firefox")
public class FirefoxConfiguration extends LocalConfiguration<FirefoxDriver> {

    public FirefoxConfiguration() {
        super(DesiredCapabilities.firefox(), FirefoxDriver::new);
    }

}
