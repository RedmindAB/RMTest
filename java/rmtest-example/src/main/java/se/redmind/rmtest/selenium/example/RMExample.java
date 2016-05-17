package se.redmind.rmtest.selenium.example;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Mouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.redmind.rmtest.WebDriverWrapper;
import se.redmind.rmtest.runners.WebDriverRunner;

@RunWith(WebDriverRunner.class)
public class RMExample {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final WebDriverWrapper<?> driverWrapper;
    private final WebDriver driver;
    private final RMNav tNavPage;
    private final RmMobileNav tMobNav;

    public RMExample(WebDriverWrapper<?> driverWrapper) throws Exception {
        this.driverWrapper = driverWrapper;
        String startUrl = "http://www.redmind.se";
        driver = driverWrapper.getDriver();
        tNavPage = new RMNav(driver, startUrl);
        tMobNav = new RmMobileNav(driver, startUrl);
    }

    @Test
    public void management() throws Exception {
        //Mobile
        if (driver.findElement(By.className("mobile-menu-wrapper")).isDisplayed()) {
            //Andriod devices
            if (driverWrapper.getCapability().getPlatform() == Platform.ANDROID) {
                tMobNav.openMobileMenu();

                tMobNav.clickOnAndroidMenu("Tjänster", "Management");

                tMobNav.assertPageTitle("Management");
                logger.info("Page title is: " + driver.getTitle());
            } else { // Mobile sites on desktop
                tMobNav.openMobileMenu();
                tMobNav.clickOnMobileMenu("Tjänster", "Management");

                tMobNav.assertPageTitle("Management");
                logger.info("Page title is: " + driver.getTitle());
            }
        } else { //desktop
            tNavPage.clickOnSubmenu("tjanster", "management");

            tNavPage.assertPageTitle("Management");
            logger.info("Page title is: " + driver.getTitle());
        }
    }

    @Test
    public void TPI() throws Exception {
        //Mobile
        if (driver.findElement(By.className("mobile-menu-wrapper")).isDisplayed()) {
            if (driverWrapper.getCapability().getPlatform() == Platform.ANDROID) {
                tMobNav.openMobileMenu();

                tMobNav.clickOnAndroidMenu("Tjänster", "TPI™ – Test process improvement");

                tMobNav.assertPageTitle("TPI™ – Test process improvement");
                logger.info("Page title is: " + driver.getTitle());
            } else { // Mobile sites on desktop
                tMobNav.openMobileMenu();
                tMobNav.clickOnMobileMenu("Tjänster", "TPI™ – Test process improvement");

                tMobNav.assertPageTitle("TPI™ – Test process improvement");
                logger.info("Page title is: " + driver.getTitle());
            }
        } else //Desktop
        if ("safari".equals(driverWrapper.getCapability().getBrowserName())) {
            //safari-code
            //Assume getDriver initialized properly.
            WebElement element = driver.findElement(By.id("Element id"));
            Mouse mouse = ((HasInputDevices) driver).getMouse();
            //mouse.mouseMove(hoverItem.getLocator());
        } else {
            tNavPage.clickOnSubmenu("tjanster", "tpi-test-process-improvement");
            tNavPage.assertPageTitle("TPI™ – Test process improvement | Redmind");

            logger.info("Page title is: " + driver.getTitle());
        }
    }

    @Test
    public void rekrytering() throws Exception {
        //Mobile
        if (driver.findElement(By.className("mobile-menu-wrapper")).isDisplayed()) {
            if (driverWrapper.getCapability().getPlatform() == Platform.ANDROID) {
                tMobNav.openMobileMenu();

                tMobNav.clickOnAndroidMenu("Tjänster", "Rekrytering");

                tMobNav.assertPageTitle("Rekrytering");
                logger.info("Page title is: " + driver.getTitle());
            } else { // Mobile sites on desktop
                tMobNav.openMobileMenu();
                tMobNav.clickOnMobileMenu("Tjänster", "Rekrytering");

                tMobNav.assertPageTitle("Rekrytering");
                logger.info("Page title is: " + driver.getTitle());
            }

        } else { //Desktop
            tNavPage.clickOnSubmenu("tjanster", "rekrytering");
            tNavPage.assertPageTitle("Rekrytering");

            logger.info("Page title is: " + driver.getTitle());
        }
    }

    @Test
    public void clientAcademy() throws Exception {
        //Mobile
        if (driver.findElement(By.className("mobile-menu-wrapper")).isDisplayed()) {
            if (driverWrapper.getCapability().getPlatform() == Platform.ANDROID) {
                tMobNav.openMobileMenu();

                tMobNav.clickOnAndroidMenu("Tjänster", "Client Academy");

                tMobNav.assertPageTitle("Client Academy");
                logger.info("Page title is: " + driver.getTitle());
            } else { // Mobile sites on desktop
                tMobNav.openMobileMenu();
                tMobNav.clickOnMobileMenu("Tjänster", "Client Academy");

                tMobNav.assertPageTitle("Client Academy");
                logger.info("Page title is: " + driver.getTitle());
            }
        } else { //Desktop
            tNavPage.clickOnSubmenu("tjanster", "client-academy");
            tNavPage.assertPageTitle("Client Academy");

            logger.info("Page title is: " + driver.getTitle());
        }
    }

    @Test
    public void konsulttjanster() throws Exception {
        //Mobile
        if (driver.findElement(By.className("mobile-menu-wrapper")).isDisplayed()) {
            if (driverWrapper.getCapability().getPlatform() == Platform.ANDROID) {
                tMobNav.openMobileMenu();

                tMobNav.clickOnAndroidMenu("Tjänster", "Konsulttjänster", "Acceptance tester");

                tMobNav.assertPageTitle("Acceptance tester");
                logger.info("Page title is: " + driver.getTitle());
            } else { // Mobile sites on desktop
                tMobNav.openMobileMenu();
                tMobNav.clickOnMobileMenu("Tjänster", "Konsulttjänster", "Acceptance tester");

                tMobNav.assertPageTitle("Acceptance tester");
                logger.info("Page title is: " + driver.getTitle());
            }
        } else { //Desktop
            tNavPage.clickOnSubmenu("tjanster", "konsulttjanster");
            assertTrue(driver.getTitle().startsWith("Konsulttjänster"));

            logger.info("Page title is: " + driver.getTitle());
        }
    }
}
