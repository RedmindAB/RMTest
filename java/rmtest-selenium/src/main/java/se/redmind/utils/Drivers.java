package se.redmind.utils;

import org.junit.Assume;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Jeremy Comte
 */
public class Drivers {

    public void ignoreAtNoConnectivityById(WebDriver driver, String url, String id) {
        ignoreAtNoConnectivityTo(driver, url, By.id(id));
    }

    public void ignoreAtNoConnectivityByClass(WebDriver driver, String url, String className) {
        ignoreAtNoConnectivityTo(driver, url, By.className(className));
    }

    public void ignoreAtNoConnectivityByXpath(WebDriver driver, String url, String xpath) {
        ignoreAtNoConnectivityTo(driver, url, By.xpath(xpath));
    }

    public void ignoreAtNoConnectivityTo(WebDriver driver, String url, By by) {
        try {
            driver.get(url);
            driverWaitElementPresent(driver, by, 10);
        } catch (NoSuchElementException | TimeoutException e) {
            Assume.assumeTrue("This driver doesn't seem to have connectivity to: " + url, false);
        }
    }

    public void driverWaitElementPresent(WebDriver driver, By pBy, int timeoutInSeconds) {
        new WebDriverWait(driver, timeoutInSeconds).until(ExpectedConditions.presenceOfElementLocated(pBy));
    }

}
