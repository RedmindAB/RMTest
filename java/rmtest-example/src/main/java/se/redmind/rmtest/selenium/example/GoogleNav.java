package se.redmind.rmtest.selenium.example;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import se.redmind.rmtest.selenium.framework.HTMLPage;
import se.redmind.utils.Try;

public class GoogleNav extends HTMLPage {

    /**
     * @param pDriver
     */
    public GoogleNav(WebDriver pDriver) throws Exception {
        super(pDriver);
        Try.toExecute(() -> driver.get("http://www.google.se")).nTimes(10);
    }

    public WebElement searchBox() {
        By searchInputField = By.name("q");
        driverFluentWait(15).until(ExpectedConditions.presenceOfElementLocated(searchInputField));
        return driver.findElement(searchInputField);
    }

    public void searchForString(String searchString) throws Exception {
        By searchResult = By.id("ires");
        WebElement searchbox = searchBox();
        searchbox.sendKeys(searchString);
        searchbox.sendKeys(Keys.RETURN);
        driverFluentWait(15).until(ExpectedConditions.presenceOfElementLocated(searchResult));
    }
}
