package se.redmind.rmtest.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Assume;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.collect.Lists;
import se.redmind.rmtest.WebDriverWrapper;
import se.redmind.utils.Try;

/**
 * @author Jeremy Comte
 */
public abstract class LocalConfiguration<WebDriverType extends WebDriver> extends DriverConfiguration<WebDriverType> {

    private boolean imAFailure;
    private final Function<DesiredCapabilities, WebDriverType> function;

    public LocalConfiguration(DesiredCapabilities baseCapabilities, Function<DesiredCapabilities, WebDriverType> function) {
        super(baseCapabilities);
        this.function = function;
    }

    @Override
    public DesiredCapabilities generateCapabilities() {
        DesiredCapabilities capabilities = super.generateCapabilities();
        capabilities.setCapability("osname", System.getProperty("os.name"));
        capabilities.setCapability("platformVersion", System.getProperty("os.arch"));
        return capabilities;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<WebDriverWrapper<WebDriverType>> createDrivers() {
        int maxRetryAttempts = 5;
        if (imAFailure) {
            Assume.assumeTrue("Since driver didn't start after  " + maxRetryAttempts + " attempts, it probably won't start now ", false);
        } else {
            WebDriverWrapper<WebDriverType> driver = Try
                .toGet(() -> new WebDriverWrapper<>(generateCapabilities(), generateDescription(), function))
                .onError((t, e) -> {
                    logger.warn("Having trouble starting webdriver for device: ", e);
                    logger.warn("Attempt " + t.currentAttempt() + " of " + t.maxAttempts());
                })
                .onLastError((t, e) -> {
                    imAFailure = true;
                    Assume.assumeTrue("Driver failed to start properly after " + maxRetryAttempts + " attempts", false);
                })
                .nTimes(maxRetryAttempts);
            if (driver != null) {
                return Lists.newArrayList(driver);
            } else {
                Assume.assumeTrue("Driver was null", false);
            }
        }
        return new ArrayList<>();
    }

}
