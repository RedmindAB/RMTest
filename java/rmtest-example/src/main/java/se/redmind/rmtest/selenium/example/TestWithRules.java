package se.redmind.rmtest.selenium.example;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.redmind.rmtest.WebDriverWrapper;
import se.redmind.rmtest.runners.WebDriverRunner;
import se.redmind.rmtest.selenium.framework.HTMLPage;
import se.redmind.rmtest.selenium.framework.RMReportScreenshot;
import se.redmind.rmtest.selenium.framework.RmTestWatcher;

import static org.junit.Assert.assertTrue;

@RunWith(WebDriverRunner.class)
public class TestWithRules {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final WebDriverWrapper<?> driverWrapper;
    private final RMReportScreenshot rmrScreenshot;

    public TestWithRules(WebDriverWrapper<?> driverWrapper) {
        this.driverWrapper = driverWrapper;
        this.rmrScreenshot = new RMReportScreenshot(driverWrapper);
    }

    @Rule
    public RmTestWatcher ruleExample = new RmTestWatcher();

    @Before
    public void beforeTest() {
        ruleExample.setDriver(driverWrapper);
    }

    @Test
    public void test() throws Exception {
        logger.info("StartOfTest");
        HTMLPage navPage = new HTMLPage(this.driverWrapper.getDriver());

        navPage.getDriver().get("http://www.comaround.se");
        assertTrue(false);
    }

}
