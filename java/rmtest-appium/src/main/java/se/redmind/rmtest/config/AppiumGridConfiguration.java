package se.redmind.rmtest.config;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

/**
 * @author Jeremy Comte
 */
@JsonTypeName("appium-grid")
public class AppiumGridConfiguration extends GridConfiguration {

    @Override
    protected RemoteWebDriver createGridWebDriver(URL driverUrl, DesiredCapabilities otherCapabilities) {
        String platformName = (String) otherCapabilities.getCapability("platformName");
        if (null != platformName) {
            List<String> elements = Arrays.asList(platformName.toUpperCase().split("\\s"));
            if (elements.contains("ANDROID")) {
                return new AndroidDriver<>(driverUrl, otherCapabilities);
            }
            if (elements.contains("IOS")) {
                return new IOSDriver<>(driverUrl, otherCapabilities);
            }
        }
        return super.createGridWebDriver(driverUrl, otherCapabilities);
    }



}
