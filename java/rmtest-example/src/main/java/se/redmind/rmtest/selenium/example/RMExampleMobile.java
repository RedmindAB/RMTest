package se.redmind.rmtest.selenium.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.redmind.rmtest.WebDriverWrapper;
import se.redmind.rmtest.runners.FilterDrivers;
import se.redmind.rmtest.runners.WebDriverRunner;
import se.redmind.rmtest.selenium.framework.Browser;

@RunWith(WebDriverRunner.class)
@FilterDrivers(browsers = Browser.Firefox, platforms = Platform.MAC)
public class RMExampleMobile {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final WebDriver tDriver;
    public final WebDriverWrapper<?> driverWrapper;
    public WebDriverWait wait;
    private final RmMobileNav tMobNav;

    public RMExampleMobile(WebDriverWrapper<?> driverWrapper) throws Exception {
        this.driverWrapper = driverWrapper;
        tDriver = driverWrapper.getDriver();
        tMobNav = new RmMobileNav(tDriver, "http://www.redmind.se");
    }

    @Test
    public void tpi() throws Exception {
        wait = new WebDriverWait(tDriver, 1);
        logger.info("Driver:" + tDriver);
        tMobNav.openMobileMenu();
        tMobNav.driverWaitElementPresent(By.linkText("Tjänster"), 1);
        tMobNav.openTpi("Tjänster", "TPI™ – Test process improvement");
        tMobNav.assertPageTitle("TPI™ – Test process improvement");
        tMobNav.driverFluentWait(2);
        logger.info("Page title is: " + tDriver.getTitle());
    }
}
