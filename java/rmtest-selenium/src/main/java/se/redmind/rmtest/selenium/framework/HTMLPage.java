package se.redmind.rmtest.selenium.framework;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author petost
 */
public class HTMLPage {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected WebDriver driver;

    /**
     * @param pDriver WebDriver
     */
    public HTMLPage(final WebDriver pDriver) {
        this.driver = pDriver;
    }

    /**
     * @return WebDriver
     */
    public WebDriver getDriver() {
        return this.driver;
    }

    /**
     * @param timeoutInSeconds int
     * @return 
     */
    private WebDriverWait driverWait(int timeoutInSeconds) {
        return new WebDriverWait(this.driver, timeoutInSeconds);
    }

    public FluentWait<WebDriver> driverFluentWait(int timeoutInSeconds) {
        FluentWait<WebDriver> fw = null;
        for (int i = 0; i < 10; i++) {
            try {
                fw = new FluentWait<>(this.driver).withTimeout(timeoutInSeconds, TimeUnit.SECONDS);
                fw.ignoring(WebDriverException.class, ClassCastException.class);
                fw.ignoring(NoSuchElementException.class);
                return fw;
            } catch (Exception e) {
                if (i >= 9) {
                    logger.warn("driverFluentWait Failed attempt : " + i + "/n" + e);
                }
            }
        }
        if (fw == null) {
            throw new WebDriverException("driverFluentWait failed after ten attempts");
        } else {
            return fw;
        }
    }

    /**
     * @param locator
     * @param timeoutInSeconds
     */
    public void driverWaitClickable(By locator, int timeoutInSeconds) {
        for (int i = 0; i < 10; i++) {
            try {
                driverFluentWait(timeoutInSeconds).until(ExpectedConditions.elementToBeClickable(locator));   // changed to driverFluentWait to ignore WebDriverExceptions braking the wait
                break;
            } catch (Exception e) {
                if (i >= 9) {
                    logger.error("driverWaitClickable exception: " + e);
                }
            }
        }

    }

    /**
     * @param condition
     * @param timeoutInSeconds
     */
    public boolean driverFluentWaitForCondition(ExpectedCondition<?> condition, int timeoutInSeconds) {
        for (int i = 0; i < 10; i++) {
            try {
                driverFluentWait(timeoutInSeconds).until(condition);   // changed to driverFluentWait to ignore WebDriverExceptions braking the wait
                return true;
            } catch (WebDriverException e) {
                logger.warn("Caught a WebDriverException on driverFluentWaitForCondition try " + i + ": " + e.getMessage(), e);
            } catch (Exception e) {
                if (i == 9) {
                    logger.warn("unexpected exception: " + e.getMessage(), e);
                }
            }
        }
        return false;
    }

    /**
     * @param pBy
     * @param timeoutInSeconds
     */
    public void driverWaitElementPresent(By pBy, int timeoutInSeconds) {
        driverWait(timeoutInSeconds).until(ExpectedConditions.presenceOfElementLocated(pBy));
    }

    /**
     * NB: might not work as expected: the predicate passed to until seems to be called once, and only once.
     */
    protected void waitUntilDomReady() throws Exception {
        driverFluentWait(45).until((org.openqa.selenium.WebDriver webDriver) -> {
            JavascriptExecutor js = (JavascriptExecutor) webDriver;
            String result = (String) js.executeScript("return document.readyState");
            return "complete".equalsIgnoreCase(result);
        });
    }

    public void assertPageTitle(String expPageTitle) throws Exception {
        logger.info("Try to assert page title: '" + expPageTitle + "'");

        String expPageTitleLow = expPageTitle.toLowerCase();
        String pageTitle = "--- Page not loaded ---";

        for (int i = 0; i < 10; i++) {
            try {
                driverFluentWait(6).until(ExpectedConditions.titleContains(expPageTitle));
                pageTitle = driver.getTitle().toLowerCase();
                logger.info(">>> Compare to page title: " + pageTitle);  // pageTitle
                break;
            } catch (Exception e) {
                if (i >= 9) {
                    logger.error("pageTitle: " + pageTitle);
                    logger.error("----- AssertPageTitle Exception: " + e.getMessage());
                }
                TimeUnit.MILLISECONDS.sleep(50);
            }
        }
        assertTrue(pageTitle.contains(expPageTitleLow));
    }

    public boolean pageTitleContains(String expPageTitle) throws Exception {
        logger.info("Try to assert page title contains: '" + expPageTitle + "'");

        boolean result = false;
        for (int i = 0; i < 10; i++) {
            try {
                logger.info(">>> Compare to page title: " + driver.getTitle());
                result = driver.getTitle().contains(expPageTitle);
                break;
            } catch (Exception e) {
                if (i >= 9) {
                    logger.error("pageTitleContains exception: " + e);
                }
                TimeUnit.MILLISECONDS.sleep(50);
            }
        }
        return result;
    }

    public boolean pageUrlContains(String articleId) throws Exception {
        logger.info("Try to assert page url: " + articleId);
        for (int i = 0; i < 10; i++) {
            try {
                logger.info(">>>Compare to page url: ");   // TODO: concatenate articleId
                boolean b = driver.getTitle().contains(articleId);
                return b;
            } catch (Exception e) {
                if (i >= 9) {
                    logger.error("pageTitleContains exception: " + e);
                }
                TimeUnit.MILLISECONDS.sleep(50);
            }
        }
        return false;
    }

    public void assertPageContains(By locator, String expText) throws Exception {
        logger.info("Try to assert page contains: " + expText);

        for (int i = 0; i < 10; i++) {
            try {
                driverFluentWait(1).until(ExpectedConditions.textToBePresentInElementLocated(locator, expText));
                break;
            } catch (Exception e) {
                if (i >= 9) {
                    logger.error("----- assertPageContains Exception: " + e);
                }
                TimeUnit.MILLISECONDS.sleep(50);
            }
        }
        assertTrue(driver.findElement(locator).getText().contains(expText));
    }

    public void spinnerClickBy(By path) throws Exception {
        logger.info("By: " + path);
        WebElement menuItem;
        for (int i = 0; i < 10; i++) {
            try {
                menuItem = driver.findElement(path);
                menuItem.getLocation();
                driverFluentWait(1).until(ExpectedConditions.visibilityOf(menuItem));
                menuItem.getLocation();
                menuItem.click();
                break;
            } catch (Exception e) {
                if (i >= 9) {
                    logger.error("spinnerClickBy exception: " + e);
                }
                TimeUnit.MILLISECONDS.sleep(50);
            }
        }
    }

    public String getTitle() {
        return driver.getTitle();
    }

}
