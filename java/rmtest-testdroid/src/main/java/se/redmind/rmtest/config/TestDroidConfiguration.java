package se.redmind.rmtest.config;

import java.net.URL;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.testdroid.api.DefaultAPIClient;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import se.redmind.rmtest.TestDroidDriverWrapper;

/**
 * @author Jeremy Comte
 */
@JsonTypeName("testdroid")
public class TestDroidConfiguration extends AppiumConfiguration {

    @JsonProperty
    public String cloudUrl = "https://cloud.testdroid.com";

    @JsonProperty
    public int maxDevices = 1000;

    public TestDroidConfiguration() {
        this.serverUrl = "https://appium.testdroid.com";
    }

    @Override
    protected TestDroidDriverWrapper createDriver(URL url) {
        Preconditions.checkArgument(username != null && password != null, "testdroid requires the credentials to be given in the configuration");
        DesiredCapabilities capabilities = generateCapabilities();
        capabilities.setCapability("testdroid_username", username);
        capabilities.setCapability("testdroid_password", password);
        return new TestDroidDriverWrapper(this, new DefaultAPIClient(cloudUrl, username, password), capabilities, generateDescription(),
            (otherCapabilities) -> {
                otherCapabilities.asMap().forEach((key, value) -> capabilities.setCapability(key, value));
                if ("Android".equalsIgnoreCase((String) capabilities.getCapability("platformName"))) {
                    return new AndroidDriver<>(url, capabilities);
                } else {
                    return new IOSDriver<>(url, capabilities);
                }
            });
    }
}
