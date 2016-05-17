package se.redmind.rmtest.selenium.example;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.google.common.base.MoreObjects;
import se.redmind.rmtest.selenium.framework.HTMLPage;

public class RMNav extends HTMLPage {

    /**
     * @param pDriver
     */
    public RMNav(WebDriver pDriver, String serverUrl) throws Exception {
        super(pDriver);

        for (int i = 0; i < 10; i++) {
            try {
                driver.get(MoreObjects.firstNonNull(serverUrl, "http://www.redmind.se"));
                break;
            } catch (Exception e) {
                logger.error(i + " AbMobileNav: " + e.getMessage(), e);
                TimeUnit.MILLISECONDS.sleep(500);
            }
        }
    }

    public WebDriver getMobileDriver() {
        return driver;
    }

    public String getCssSelector(String pText) {
        return "a[href*='" + pText + "']";
    }

    public void clickOnMenu(String pMenuText) throws Exception {
        logger.info("Clicking: " + pMenuText);
        driver.findElement(By.cssSelector(getCssSelector(pMenuText))).click();
    }

    public void openMobileMenu() throws Exception {
        logger.info("Opening menu");
        driver.findElement(By.className("mobile-menu-control")).click();
    }

    public void clickOnSubmenu(String pMenuText, String pSubMenuText)
        throws Exception {
        Actions builder = new Actions(driver);

        builder.moveToElement(driver.findElement(By.cssSelector(getCssSelector(pMenuText)))).perform();

        logger.info("Hovering over " + pMenuText);

        driverFluentWait(20).until(ExpectedConditions.elementToBeClickable(By.className("sub-menu")));

        logger.info("Pressing " + pSubMenuText);
        driver.findElement(By.cssSelector(getCssSelector(pSubMenuText))).click();
        driverFluentWait(20);
    }

    public void clickOnSubmenu(String pMenuText, String pSubMenuText,
                               String pSubSubMenuText) throws Exception {
        Actions builder = new Actions(driver);

        builder.moveToElement(driver.findElement(By.cssSelector(getCssSelector(pMenuText)))).perform();

        logger.info("Hovering over " + pMenuText);

        driverFluentWait(20).until(ExpectedConditions.elementToBeClickable(By.className("sub-menu")));

        builder.moveToElement(driver.findElement(By.cssSelector(getCssSelector(pSubMenuText)))).perform();

        logger.info("Hovering over " + pSubMenuText);

        driverFluentWait(20).until(ExpectedConditions.elementToBeClickable(By.className("sub-menu")));

        logger.info("Pressing " + pSubSubMenuText);
        driver.findElement(By.cssSelector(getCssSelector(pSubSubMenuText))).click();

        driverFluentWait(20);
    }
}
