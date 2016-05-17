package se.redmind.rmtest.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import se.redmind.rmtest.AppiumDriverWrapper;
import se.redmind.rmtest.WebDriverWrapper;

/**
 * @author Jeremy Comte
 */
@JsonTypeName("appium")
public class AppiumConfiguration extends DriverConfiguration<AppiumDriver<WebElement>> {

    @JsonProperty
    @NotNull
    public String serverUrl;

    @JsonProperty
    public String username;

    @JsonProperty
    public String password;

    @JsonProperty
    public String appPath;

    public AppiumConfiguration() {
        super(new DesiredCapabilities());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<WebDriverWrapper<AppiumDriver<WebElement>>> createDrivers() {
        try {
            return Lists.newArrayList(createDriver(new URL(serverUrl + "/wd/hub")));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected AppiumDriverWrapper createDriver(URL url) {
        return new AppiumDriverWrapper(this, generateCapabilities(), generateDescription(), capabilities -> {
            if ("Android".equalsIgnoreCase((String) capabilities.getCapability("platformName"))) {
                return new AndroidDriver<>(url, capabilities);
            } else {
                return new IOSDriver<>(url, capabilities);
            }
        });
    }

}
