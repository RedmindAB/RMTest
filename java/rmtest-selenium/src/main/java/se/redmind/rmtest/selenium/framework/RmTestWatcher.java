package se.redmind.rmtest.selenium.framework;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.redmind.rmtest.WebDriverWrapper;

public class RmTestWatcher extends TestWatcher {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private WebDriverWrapper<?> driverWrapper;

    @Override
    protected void failed(Throwable e, Description description) {
        logger.warn("Only used when a test fails. Failing Method: " + description.getClassName() + "." + description.getMethodName());
        String methodName = description.getMethodName();
        if (methodName.contains("[")) {
            methodName = methodName.replaceFirst("(.*)\\[.*", "$1");
        }
        new RMReportScreenshot(driverWrapper).takeScreenshot(description.getClassName(), methodName, "FailedTestcase");
    }

    public void setDriver(WebDriverWrapper<?> driverWrapper) {
        this.driverWrapper = driverWrapper;
    }
}
