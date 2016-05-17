package se.redmind.rmtest.selenium.example;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.google.common.base.MoreObjects;
import se.redmind.rmtest.selenium.framework.HTMLPage;
import se.redmind.utils.Try;

public class RmMobileNav extends HTMLPage {

    private Actions builder;

    /**
     * @param pDriver
     */
    public RmMobileNav(WebDriver pDriver, String serverUrl) throws Exception {
        super(pDriver);
        Try.toExecute(() -> driver.get(MoreObjects.firstNonNull(serverUrl, "http://www.redmind.se"))).delayRetriesBy(500).nTimes(10);
    }

    public void openMobileMenu() {
        driverWaitElementPresent(By.className("mobile-menu-control"), 60);
        driver.findElement(By.className("mobile-menu-control")).click();
        logger.info("Opening menu");
        driverFluentWait(20).until(ExpectedConditions.presenceOfElementLocated(By.id("menu-main-menu-1")));
    }

    public void openTpi(String Menu, String SubMenu) throws Exception {
        driverFluentWait(20).until(ExpectedConditions.visibilityOfElementLocated(By.linkText(Menu)));

        builder = new Actions(driver);
        builder.moveToElement(driver.findElement(By.linkText(Menu))).perform();

        logger.info("Pressing " + Menu);
        driver.findElement(By.linkText(SubMenu)).click();
    }

    public void openManag(String pMenu, String pSubMenu) throws Exception {
        driverFluentWait(20).until(ExpectedConditions.presenceOfElementLocated(By.linkText(pMenu)));
        builder = new Actions(driver);
        builder.moveToElement(driver.findElement(By.linkText(pMenu))).perform();

        logger.info("Hovering over " + pMenu);
        logger.info("Pressing " + pSubMenu);
        driver.findElement(By.linkText(pSubMenu)).click();
    }

    public void openRyk(String pText, String pSubText) throws Exception {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        builder = new Actions(driver);
        builder.moveToElement(driver.findElement(By.linkText(pText))).perform();
        logger.info("Pressing " + pSubText);
        driver.findElement(By.linkText(pSubText)).click();
    }

    public void openClAc(String Text, String SubText) throws Exception {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        builder = new Actions(driver);
        builder.moveToElement(driver.findElement(By.linkText(Text))).perform();
        logger.info("Pressing " + SubText);
        driver.findElement(By.linkText(SubText)).click();
    }

    public void openKTj(String menu, String subMenu1, String subMenu2)
        throws Exception {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        builder = new Actions(driver);
        builder.moveToElement(driver.findElement(By.linkText(menu))).perform();
        builder.moveToElement(driver.findElement(By.linkText(subMenu1)))
            .perform();
        Thread.sleep(500L);
        driver.findElement(By.linkText(subMenu2)).click();

    }

    public void clickOnMobileMenu(String Text, String SubText) {
        driverWaitClickable(By.linkText(Text), 20);

        builder = new Actions(driver);
        builder.moveToElement(driver.findElement(By.linkText(Text))).perform();
        logger.info("Hovering over " + Text);

        driverWaitClickable(By.linkText(SubText), 20);

        driver.findElement(By.linkText(SubText)).click();
        logger.info("Pressing " + SubText);

        driverWaitElementPresent(By.className("mobile-menu-control"), 60);
    }

    public void clickOnMobileMenu(String Text, String SubText, String SubSubText) {
        driverWaitClickable(By.linkText(Text), 20);

        builder = new Actions(driver);
        builder.moveToElement(driver.findElement(By.linkText(Text))).perform();
        logger.info("Hovering over " + Text);

        driverWaitClickable(By.linkText(SubText), 20);

        builder.moveToElement(driver.findElement(By.linkText(SubText))).perform();
        logger.info("Hovering over " + SubText);

        driverWaitClickable(By.linkText(SubText), 20);

        driver.findElement(By.linkText(SubSubText)).click();
        logger.info("Pressing " + SubSubText);

        driverWaitElementPresent(By.className("mobile-menu-control"), 60);
    }

    public void clickOnAndroidMenu(String text, String subText) throws InterruptedException {
        driverFluentWaitForCondition(ExpectedConditions.visibilityOfElementLocated(By.linkText(text)), 20);

        driver.findElement(By.linkText(text)).click();
        driver.findElement(By.linkText(subText)).click();
    }

    public void clickOnAndroidMenu(String text, String subText, String subSubText) throws InterruptedException {
        driverFluentWaitForCondition(ExpectedConditions.visibilityOfElementLocated(By.linkText(text)), 20);

        driver.findElement(By.linkText(text)).click();
        driver.findElement(By.linkText(subText)).click();
        driver.findElement(By.linkText(subSubText)).click();
    }
}
