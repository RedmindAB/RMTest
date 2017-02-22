package se.redmind.rmtest.cucumber.web;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cucumber.api.java.en.And;

import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import cucumber.api.DataTable;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import se.redmind.rmtest.WebDriverWrapper;
import se.redmind.rmtest.config.Configuration;
import se.redmind.rmtest.selenium.grid.GridWebDriver;
import se.redmind.utils.Try;

/**
 * @author Jeremy Comte
 */
public class WebDriverSteps {

    private static final AtomicInteger LOCAL_COUNTER = new AtomicInteger();

    private static final String THAT = "(?:that )?";
    private static final String THE_USER = "(?:.*)?";
    private static final String THE_ELEMENT = "(?:(?:the |an |a )?(?:frame|button|element|field|checkbox|radio|value)?(?:s)?)?";
    private static final String DO_SOMETHING = "(click|clear|submit|select|hover)(?:s? (?:on|in))?";
    private static final String INPUT = "(?:input|type)(?:s? (?:on|in))?";
    private static final String IDENTIFIED_BY = "(?:with (?:the )?)?(name(?:d)?|id|xpath|class|css|(?:partial )?link text|tag)? ?\"(.*)\"";
    private static final String THE_ELEMENT_IDENTIFIED_BY = THE_ELEMENT + " ?" + IDENTIFIED_BY;
    private static final String THIS_ELEMENT = "(?:(?:this|the|an) element(?:s)?|it(?:s)?)";
    private static final String MATCHES = "(!)?(reads|returns|is|equals|contains|starts with|ends with|links to|matches)";
    private static final String QUOTED_CONTENT = "\"([^\"]*)\"";
    private static final String VARIABLE = "[_a-zA-Z][_\\-.\\w]*";
    private static final String NAMED = "\"(" + VARIABLE + ")\"";

    private static final Pattern ALIAS = Pattern.compile("(.*)(?:\\$\\{([\\w()]+)})(.*)");

    private final Map<String, By> aliasedLocations = new LinkedHashMap<>();
    private final Map<String, String> aliasedValues = new LinkedHashMap<>();
    private final WebDriverWrapper<WebDriver> driverWrapper;
    private final WebDriver driver;
    private WebElement element;
    private By elementLocation;

    public WebDriverSteps(WebDriverWrapper<WebDriver> driverWrapper) {
        this.driverWrapper = driverWrapper;
        this.driver = driverWrapper.getDriver();
    }

    public WebDriver getDriver() {
        return driver;
    }

    @After
    public void after() {
        if (!driverWrapper.reuseDriverBetweenTests()) {
            driverWrapper.stopDriver();
        }
    }

    // Helpers
    @When("^" + THAT + THE_USER + " know(?:s)?( the value of)? " + THE_ELEMENT_IDENTIFIED_BY + " as " + NAMED + "$")
    public void that_we_know_the_element_named_as(String asValue, String type, String id, String alias) {
        alias = valueOf(alias);
        if (type != null && !"value".equals(type)) {
            if (asValue == null) {
                aliasedLocations.put(alias, by(type, id));
            } else {
                aliasedValues.put(alias, getValueOf(find(by(type, id))));
            }
        } else {
            aliasedValues.put(alias, valueOf(id));
        }
    }

    @Then("^" + THAT + THE_USER + " know(?:s)? " + THIS_ELEMENT + " attribute " + QUOTED_CONTENT + " as " + NAMED + "$")
    public void we_know_its_attribute_as(String attribute, String alias) {
        aliasedValues.put(alias, element.getAttribute(attribute));
    }

    @Then("^" + THAT + THE_USER + " know(?:s)? " + THIS_ELEMENT + " property " + QUOTED_CONTENT + " as " + NAMED + "$")
    public void we_know_its_property_as(String property, String alias) {
        aliasedValues.put(alias, element.getCssValue(property));
    }

    @Given("^th(?:is|ose|ese) alias(?:es)?:$")
    public void these_aliases(List<Map<String, String>> newAliases) {
        newAliases.forEach(alias -> {
            String value = alias.get("value");
            if (value.matches(VARIABLE)) {
                aliasedLocations.put(value, by(alias.get("type"), alias.get("id")));
            } else {
                throw new IllegalArgumentException("alias " + value + " must match " + NAMED);
            }
        });
    }

    @Given("^the aliases defined in the file " + QUOTED_CONTENT + "$")
    public void the_aliases_defined_in_the_file(String fileName) throws IOException {
        Splitter splitter = Splitter.on("|").trimResults().omitEmptyStrings();
        List<String> lines = Files.readLines(new File(fileName), Charset.defaultCharset());
        List<List<String>> rows = lines.stream().map(splitter::splitToList).collect(Collectors.toList());
        these_aliases(DataTable.create(rows).asMaps(String.class, String.class));
    }

    // Navigates
    @When("^" + THAT + THE_USER + " navigate(?:s)? to " + QUOTED_CONTENT + "$")
    public void we_navigate_to(String url) {
        String resolvedUrl = valueOf(url);
        driver.navigate().to(resolvedUrl);
        Assert.assertTrue("couldn't navigate to url '" + resolvedUrl + "'", getDriver().getCurrentUrl().startsWith("http"));
    }

    @When("^" + THAT + THE_USER + " (?:(?:go(?:es) )?(back|forward|refresh(?:es)?))$")
    public void we_navigate(String action) {
        switch (action) {
            case "back":
                driver.navigate().back();
                break;
            case "forward":
                driver.navigate().forward();
                break;
            case "refresh":
                driver.navigate().refresh();
                break;
        }
    }

    @Given("^" + THAT + THE_USER + " switch(?:es)? to the frame with (?:name|id) " + QUOTED_CONTENT + "$")
    public void that_we_switch_to_the_frame_with(String nameOrId) {
        driver.switchTo().frame(nameOrId);
    }

    @Given("^" + THAT + THE_USER + " switch(?:es)? to the default content$")
    public void that_we_switch_to_the_default_content() {
        driver.switchTo().defaultContent();
    }

    @Given("^" + THAT + THE_USER + " switch(?:es)? to the parent frame$")
    public void that_we_switch_to_the_parent_frame() {
        if (driver instanceof PhantomJSDriver) {
            throw new IllegalStateException("PhantomJSDriver doesn't support the parent frame location, see https://github.com/SeleniumHQ/selenium/issues/1737");
        }
        driver.switchTo().parentFrame();
    }

    @When("^" + THAT + THE_USER + " switch(?:es)? to the frame " + NAMED + "$")
    public void that_we_switch_to_the_frame(String alias) {
        driver.switchTo().frame(find(by(null, alias)));
    }

    @And("^" + THAT + THE_USER + " switch(?:es)? to the frame with index (\\d+)$")
    public void that_we_switch_to_the_frame_with_index(int index) {
        driver.switchTo().frame(index);
    }

    // Cookie Options
    @Given("^" + THAT + THE_USER + " add(?:s)? th(?:is|ose) cookie(?:s)?:$")
    public void that_we_add_those_cookies(List<Map<String, String>> data) {
        data.forEach(cookieAsMap -> {
            Cookie.Builder builder = new Cookie.Builder(cookieAsMap.get("name"), cookieAsMap.get("value"));
            if (cookieAsMap.containsKey("domain")) {
                builder.domain(cookieAsMap.get("domain"));
            }
            if (cookieAsMap.containsKey("path")) {
                builder.path(cookieAsMap.get("path"));
            }
            if (cookieAsMap.containsKey("expiry")) {
                builder.expiresOn(Date.valueOf(cookieAsMap.get("expiry")));
            }
            if (cookieAsMap.containsKey("isSecure")) {
                builder.isSecure(Boolean.valueOf(cookieAsMap.get("isSecure")));
            }
            if (cookieAsMap.containsKey("isHttpOnly")) {
                builder.isHttpOnly(Boolean.valueOf(cookieAsMap.get("isHttpOnly")));
            }
            driver.manage().addCookie(builder.build());
        });
    }

    @Given("^" + THAT + THE_USER + " delete(?:s)? the cookie \"([^\"]*)\"$")
    public void that_we_delete_the_cookie(String name) {
        driver.manage().deleteCookieNamed(name);
    }

    @Given("^" + THAT + THE_USER + " delete(?:s)? all the cookies$")
    public void that_we_delete_all_the_cookies() {
        driver.manage().deleteAllCookies();
    }

    // Actions
    @When("^" + THAT + THE_USER + " " + DO_SOMETHING + " " + THE_ELEMENT_IDENTIFIED_BY + "$")
    public void that_we_do_something_on_the_element_identified_by(String action, String type, String id) {
        find(by(type, id));
        that_we_do_something_on_the_element_identified_by(action);
    }

    @When("^" + THAT + THE_USER + " " + DO_SOMETHING + " " + THIS_ELEMENT + "$")
    public void that_we_do_something_on_the_element_identified_by(String action) {
        switch (action) {
            case "click":
                driverWrapper.waitForCondition(ExpectedConditions.elementToBeClickable(element));
                action().moveToElement(element).click().perform();
                break;
            case "clear":
                element.clear();
                break;
            case "submit":
                element.submit();
                break;
            case "hover":
                new Actions(driver).moveToElement(element).perform();
                break;
        }
    }

    @Given("^" + THAT + THE_USER + " maximize(?:s)? the window$")
    public void that_we_maximize_the_window() throws InterruptedException {
        the_window_is_maximized();
    }

    @When("^" + THAT + "the window is maximized$")
    public void the_window_is_maximized() throws InterruptedException {
        driver.manage().window().maximize();
        if (System.getProperty("os.name").startsWith("Mac")) {
            driverWrapper.getDriver().manage().window().setSize(new Dimension(1280, 950));
        }
    }

    @When("^" + THAT + THE_USER + " (?:press[es]?)(?:[es])? (.*)$")
    public void that_we_press(Keys keys) {
        element.sendKeys(keys);
    }

    @When("^" + THAT + THE_USER + " " + INPUT + " " + QUOTED_CONTENT + "$")
    public void that_we_input(String content) {
        content = valueOf(content);
        if (element.getTagName().equals("input") && "file".equals(element.getAttribute("type"))) {
            if (driverWrapper.getDriver() instanceof GridWebDriver) {
                ((RemoteWebElement) element).setFileDetector(new LocalFileDetector());
            }
            element.sendKeys(content);
        } else {
            new Actions(driver).moveToElement(element).click().sendKeys(content).build().perform();
        }
    }

    @When("^" + THAT + THE_USER + " " + INPUT + " " + QUOTED_CONTENT + " in " + THE_ELEMENT_IDENTIFIED_BY + "$")
    public void that_we_input_in_the_element_identified_by(String content, String type, String id) {
        find(by(type, id));
        that_we_input(content);
    }

    @When("^" + THAT + THE_USER + " wait(?:s)? (\\d+) (\\w+)")
    public void we_wait(int amount, TimeUnit timeUnit) throws InterruptedException {
        timeUnit.sleep(amount);
    }

    @When("^" + THAT + THE_USER + " execute(?:s)? " + QUOTED_CONTENT + " on " + THE_ELEMENT_IDENTIFIED_BY + "$")
    public void that_we_execute_on_the_element_identified_by(String javascript, String type, String id) {
        find(by(type, id));
        that_we_execute_on_this_element(javascript);
    }

    @When("^" + THAT + THE_USER + " execute(?:s)? " + QUOTED_CONTENT + " on " + THIS_ELEMENT + "$")
    public void that_we_execute_on_this_element(String javascript) {
        ((JavascriptExecutor) driver).executeScript(valueOf(javascript) + ";", element);
    }

    // Assertions
    @Then("^" + THAT + "the title " + MATCHES + " " + QUOTED_CONTENT + "$")
    public void the_title_matches(String not, String assertType, String expectedValue) {
        assertString(assertType, getNotNullOrEmpty(driver::getTitle), not == null, expectedValue);
    }

    @Then("^" + THAT + "the page content " + MATCHES + " " + QUOTED_CONTENT + "$")
    public void the_page_content_is(String not, String assertType, String expectedValue) {
        assertString(assertType, find(By.tagName("body")).findElement(By.tagName("pre")).getText(), not == null, expectedValue);
    }

    @Then("^" + THAT + THE_ELEMENT_IDENTIFIED_BY + " " + MATCHES + " " + QUOTED_CONTENT + "$")
    public void that_the_element_at_id_matches(String type, String id, String not, String assertType, String expectedValue) {
        if (type == null && !aliasedLocations.containsKey(id)) {
            assertString(assertType, id, not == null, expectedValue);
        } else {
            assertElement(assertType, find(by(type, id)), not == null, expectedValue);
        }
    }

    @Then("^" + THAT + THE_ELEMENT_IDENTIFIED_BY + " (!)?(?:is present|exists)$")
    public void the_element_with_id_is_present(String type, String id, String not) {
        if (not == null) {
            find(by(type, id));
        } else {
            Assert.assertTrue(doesNotFind(by(type, id)));
        }
    }

    @Then("^" + THAT + THE_ELEMENT_IDENTIFIED_BY + " (!)?is displayed$")
    public void the_element_with_id_is_displayed(String type, String id, String not) {
        find(by(type, id));
        this_element_is_displayed(not);
    }

    @Then("^" + THAT + THIS_ELEMENT + " (!)?is displayed$")
    public void this_element_is_displayed(String not) {
        assertCondition(() -> element.isDisplayed(), not == null);
    }

    @Then("^" + THAT + THE_ELEMENT_IDENTIFIED_BY + " (!)?is enabled$")
    public void the_element_with_id_is_enabled(String type, String id, String not) {
        find(by(type, id));
        this_element_is_enabled(not);
    }

    @Then("^" + THAT + THIS_ELEMENT + " (!)?is enabled$")
    public void this_element_is_enabled(String not) {
        assertCondition(() -> element.isEnabled(), not == null);
    }

    @Then("^" + THAT + THE_ELEMENT_IDENTIFIED_BY + " (!)?is selected$")
    public void the_element_with_id_is_selected(String type, String id, String not) {
        find(by(type, id));
        this_element_is_selected(not);
    }

    @Then("^" + THAT + THIS_ELEMENT + " (!)?is selected$")
    public void this_element_is_selected(String not) {
        assertCondition(() -> element.isSelected(), not == null);
    }

    @Then("^" + THAT + "the current url " + MATCHES + " " + QUOTED_CONTENT + "$")
    public void the_current_url_ends_with(String not, String assertType, String expectedValue) {
        assertString(assertType, driver.getCurrentUrl(), not == null, expectedValue);
    }

    @Then("^" + THAT + "executing " + QUOTED_CONTENT + " " + MATCHES + " \"?(.+)\"?$")
    public void executing_returns(String javascript, String not, String assertType, String expectedValue) {
        String value = String.valueOf(((JavascriptExecutor) driver).executeScript(valueOf(javascript)));
        assertString(assertType, value, not == null, expectedValue);
    }

    @Then("^" + THAT + THE_USER + " execute(?:s)? " + QUOTED_CONTENT + " as " + NAMED + "$")
    public void we_execute_as(String javascript, String alias) {
        String value = String.valueOf(((JavascriptExecutor) driver).executeScript(valueOf(javascript)));
        aliasedValues.put(alias, value);
    }

    @Then("^" + THAT + "evaluating " + QUOTED_CONTENT + " " + MATCHES + " \"?(.+)\"?$")
    public void evaluating(String javascript, String not, String assertType, String expectedValue) {
        String value = String.valueOf(((JavascriptExecutor) driver).executeScript("return " + valueOf(javascript) + ";"));
        assertString(assertType, value, not == null, expectedValue);
    }

    @Then("^" + THAT + THE_USER + " evaluate(?:s)? " + QUOTED_CONTENT + " as " + NAMED + "$")
    public void we_evaluate_as(String javascript, String alias) {
        String value = String.valueOf(((JavascriptExecutor) driver).executeScript("return " + valueOf(javascript) + ";"));
        aliasedValues.put(alias, value);
    }

    @Then("^the amount of " + THE_ELEMENT_IDENTIFIED_BY + " equals (\\d+)$")
    public void the_amount_of_elements_with_xpath_equals(String type, String id, int amount) {
        Assert.assertEquals(amount, driver.findElements(by(type, id)).size());
    }

    @Given("^" + THAT + THE_USER + " count(?:s)? " + THE_ELEMENT_IDENTIFIED_BY + " as " + NAMED + "$")
    public void that_we_count_the_elements_with_xpath_as(String type, String id, String alias) {
        aliasedValues.put(alias, String.valueOf(driver.findElements(by(type, id)).size()));
    }

    @Then("^" + THAT + THIS_ELEMENT + " " + MATCHES + " " + QUOTED_CONTENT + "$")
    public void this_element_matches(String not, String assertType, String expectedValue) {
        refreshElement();
        assertElement(assertType, element, not == null, valueOf(expectedValue));
    }

    @Then("^" + THAT + THIS_ELEMENT + " attribute \"([^\"]*)\" " + MATCHES + " " + QUOTED_CONTENT + "$")
    public void this_element_attribute_matches(String attribute, String not, String assertType, String expectedValue) {
        assertString(assertType, element.getAttribute(attribute), not == null, expectedValue);
    }

    @Then("^" + THAT + "the attribute " + QUOTED_CONTENT + " of " + THE_ELEMENT_IDENTIFIED_BY + " " + MATCHES + " " + QUOTED_CONTENT + "$")
    public void the_attribute_of_the_element_identified_by_matches(String attribute, String type, String id, String not, String assertType, String expectedValue) {
        find(by(type, id));
        this_element_attribute_matches(attribute, not, assertType, expectedValue);
    }

    @Then("^" + THAT + THIS_ELEMENT + " property \"([^\"]*)\" " + MATCHES + " " + QUOTED_CONTENT + "$")
    public void this_element_property_matches(String property, String not, String assertType, String expectedValue) {
        assertString(assertType, element.getCssValue(property), not == null, expectedValue);
    }

    @Then("^" + THAT + "the property " + QUOTED_CONTENT + " of " + THE_ELEMENT_IDENTIFIED_BY + " " + MATCHES + " " + QUOTED_CONTENT + "$")
    public void the_property_of_the_element_identified_by_matches(String property, String type, String id, String not, String assertType, String expectedValue) {
        find(by(type, id));
        this_element_property_matches(property, not, assertType, expectedValue);
    }

    public WebElement currentElement() {
        return element;
    }

    public By currentElementLocation() {
        return elementLocation;
    }

    // private helpers
    private WebElement find(By elementLocation) {
        this.elementLocation = elementLocation;
        driverWrapper.driverFluentWait().until(ExpectedConditions.presenceOfElementLocated(elementLocation));
        element = driver.findElement(elementLocation);
        return element;
    }

    private boolean doesNotFind(By elementLocation) {
        return Try.toGet(() -> driver.findElements(elementLocation))
            .until(List::isEmpty)
            .delayRetriesBy(Configuration.current().defaultTimeOut * 100)
            .nTimes(10).isEmpty();
    }

    private void refreshElement() {
        find(elementLocation);
    }

    private Actions action() {
        return new Actions(driver);
    }

    private String get(Supplier<String> supplier) {
        return Try.toGet(supplier)
            .delayRetriesBy(Configuration.current().defaultTimeOut * 100)
            .nTimes(10);
    }

    private String getNotNullOrEmpty(Supplier<String> supplier) {
        return Try.toGet(supplier)
            .until(value -> !Strings.isNullOrEmpty(value))
            .delayRetriesBy(Configuration.current().defaultTimeOut * 100)
            .nTimes(10);
    }

    private String valueOf(String value) {
        return valueOf(value, false);
    }

    private String valueOf(String value, boolean readElementValue) {
        if (value == null) {
            return null;
        }
        if (value.equals("UUID()")) {
            return UUID.randomUUID().toString();
        }
        if (value.equals("ID()")) {
            return String.valueOf(LOCAL_COUNTER.incrementAndGet());
        }
        if (readElementValue) {
            if (aliasedValues.containsKey(value)) {
                value = aliasedValues.get(value);
            }
            if (aliasedLocations.containsKey(value)) {
                value = getValueOf(find(aliasedLocations.get(value)));
            }
        }
        Matcher matcher;
        while ((matcher = ALIAS.matcher(value)).matches()) {
            value = matcher.group(1) + valueOf(matcher.group(2), true) + matcher.group(3);
        }
        return value;
    }

    private By by(String type, String id) {
        id = valueOf(id);
        Preconditions.checkArgument(id != null);
        if (type == null) {
            return getAliasedLocation(id);
        } else {
            switch (type) {
                case "named":
                    return By.name(id);
                case "id":
                    return By.id(id);
                case "xpath":
                    return By.xpath(id);
                case "class":
                    return By.className(id);
                case "css":
                    return By.cssSelector(id);
                case "link text":
                    return By.linkText(id);
                case "partial link text":
                    return By.partialLinkText(id);
                case "tag":
                    return By.tagName(id);
                default:
                    throw new IllegalArgumentException("unknown parameter type: " + type + " value: " + id);
            }
        }
    }

    private By getAliasedLocation(String id) {
        if (aliasedLocations.containsKey(id)) {
            return aliasedLocations.get(id);
        } else {
            throw new IllegalArgumentException("unknown alias: " + id);
        }
    }

    private void assertCondition(Supplier<Boolean> condition, boolean shouldBeTrue) {
        boolean state = Try.toGet(() -> condition.get())
            .until(value -> value == shouldBeTrue)
            .delayRetriesBy(Configuration.current().defaultTimeOut * 100)
            .defaultTo(value -> !shouldBeTrue)
            .nTimes(10);
        Assert.assertEquals(shouldBeTrue, state);
    }

    private void assertElement(String assertType, WebElement element, boolean shouldBeTrue, String expected) {
        expected = valueOf(expected);
        String value;
        if (assertType.equals("links to")) {
            assertType = "is";
            value = element.getAttribute("href");
        } else if (element.getTagName().equals("input")) {
            value = element.getAttribute("value");
        } else {
            value = getValueOf(element);
        }
        assertString(assertType, value, shouldBeTrue, expected);
    }

    private String getValueOf(WebElement element) {
        return getNotNullOrEmpty(element::getText);
    }

    private void assertString(String assertType, String value, boolean shouldBeTrue, String expected) {
        value = valueOf(value);
        expected = valueOf(expected);
        switch (assertType) {
            case "reads":
            case "returns":
            case "is":
            case "equals":
                if (shouldBeTrue) {
                    Assert.assertEquals(expected, value);
                } else {
                    Assert.assertNotEquals(expected, value);
                }
                break;
            case "matches":
                Assert.assertEquals("'" + value + "' doesn't match '" + expected + "'", shouldBeTrue, value.matches(expected));
                break;
            case "contains":
                Assert.assertEquals("'" + value + "' doesn't contain '" + expected + "'", shouldBeTrue, value.contains(expected));
                break;
            case "starts with":
                Assert.assertEquals("'" + value + "' doesn't start with '" + expected + "'", shouldBeTrue, value.startsWith(expected));
                break;
            case "ends with":
                Assert.assertEquals("'" + value + "' doesn't end with '" + expected + "'", shouldBeTrue, value.endsWith(expected));
                break;
            default:
                throw new IllegalArgumentException("unknown assert type " + assertType);
        }
    }
}
