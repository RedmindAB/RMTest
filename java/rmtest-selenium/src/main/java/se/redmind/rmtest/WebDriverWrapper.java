package se.redmind.rmtest;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.junit.Assume;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jodah.typetools.TypeResolver;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import se.redmind.rmtest.config.Configuration;
import se.redmind.rmtest.runners.Capability;
import se.redmind.rmtest.runners.FilterDrivers;
import se.redmind.rmtest.selenium.framework.Browser;
import se.redmind.rmtest.selenium.grid.DriverConfig;
import se.redmind.utils.ThrowingRunnable;
import se.redmind.utils.Try;

/**
 * This is a wrapper around a ThreadLocal WebDriver instance.
 *
 * This allow us to parallelize a test on multiple webdrivers as well as using the same webdriver configuration for multiple threads at the same time.
 *
 * @author Jeremy Comte
 */
public class WebDriverWrapper<WebDriverType extends WebDriver> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Function<DesiredCapabilities, WebDriverType> function;
    private final DesiredCapabilities capabilities;
    private final String description;

    private final Set<WebDriverType> openDrivers = new LinkedHashSet<>();
    private final ThreadLocal<Boolean> isStarted = ThreadLocal.withInitial(() -> false);
    private final ThreadLocal<Boolean> isInitializing = ThreadLocal.withInitial(() -> false);
    private final ThreadLocal<WebDriverType> driverInstance = new ThreadLocal<WebDriverType>() {

        @Override
        protected WebDriverType initialValue() {
            long start = System.currentTimeMillis();
            if (isInitializing.get()) {
                throw new IllegalStateException("this driver is already being initialized, is getDriver() being called in a pre/postConfiguration hook?");
            }
            isInitializing.set(true);
            preConfigurations.forEach(preConfiguration -> {
                try {
                    preConfiguration.run();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
            WebDriverType driver;
            try {
                driver = function.apply(capabilities);
            } catch (UnreachableBrowserException | SessionNotCreatedException exception) {
                isInitializing.set(false);
                driverInstance.remove();
                logger.error(exception.getMessage());
                throw exception;
            }
            openDrivers.add(driver);
            postConfigurations.forEach(postConfiguration -> postConfiguration.accept(driver));
            isStarted.set(true);
            logger.info("Started driver [" + description + "] (took " + (System.currentTimeMillis() - start) + "ms)");
            isInitializing.set(false);
            return driver;
        }

    };
    private final Set<ThrowingRunnable> preConfigurations = new LinkedHashSet<>();
    private final Set<Consumer<WebDriverType>> postConfigurations = new LinkedHashSet<>();

    private boolean reuseDriverBetweenTests;

    public WebDriverWrapper(DesiredCapabilities capabilities, String description, Function<DesiredCapabilities, WebDriverType> function) {
        this.capabilities = capabilities;
        this.description = description;
        this.function = function;
    }

    public void addDriverConfig(DriverConfig conf) {
        addCapabilities(conf::eval, conf::config);
    }

    public void addCapabilities(BiFunction<DesiredCapabilities, String, Boolean> eval, Consumer<DesiredCapabilities> action) {
        if (eval.apply(capabilities, description)) {
            action.accept(capabilities);
        }
    }

    public void addPreConfiguration(ThrowingRunnable preConfiguration) {
        preConfigurations.add(preConfiguration);
    }

    public void addPostConfiguration(Consumer<WebDriverType> postConfiguration) {
        postConfigurations.add(postConfiguration);
    }

    public String getDescription() {
        return description;
    }

    public boolean isStarted() {
        return isStarted.get();
    }

    public WebDriverType getDriver() {
        return driverInstance.get();
    }

    public boolean reuseDriverBetweenTests() {
        return reuseDriverBetweenTests;
    }

    public void setReuseDriverBetweenTests(boolean reuseDriverBetweenTests) {
        this.reuseDriverBetweenTests = reuseDriverBetweenTests;
    }

    public void stopDriver() {
        synchronized (driverInstance) {
            if (isStarted.get()) {
                openDrivers.remove(driverInstance.get());
                logger.info("Closing driver [" + description + "]");
                try {
                    WebDriverType driver = driverInstance.get();
                    CompletableFuture.runAsync(() -> driver.quit()).get(10, TimeUnit.SECONDS);
                } catch (java.util.concurrent.TimeoutException e) {
                    logger.error("couldn't close driver [" + description + "] within 10 seconds...");
                } catch (UnreachableBrowserException | InterruptedException | ExecutionException e) {
                    logger.error(e.getMessage());
                }
                driverInstance.remove();
                isStarted.remove();
                logger.info("Driver [" + description + "] closed");
            }
        }
    }

    public void stopAllDrivers() {
        logger.info("Closing all drivers");
        openDrivers.forEach(driver -> driver.quit());
    }

    public void ignoreAtNoConnectivityById(String url, String id) {
        ignoreAtNoConnectivityTo(url, By.id(id));
    }

    public void ignoreAtNoConnectivityByClass(String url, String className) {
        ignoreAtNoConnectivityTo(url, By.className(className));
    }

    public void ignoreAtNoConnectivityByXpath(String url, String xpath) {
        ignoreAtNoConnectivityTo(url, By.xpath(xpath));
    }

    public void ignoreAtNoConnectivityTo(String url, By by) {
        try {
            getDriver().get(url);
            driverWaitElementPresent(by, 10);
        } catch (NoSuchElementException | TimeoutException e) {
            Assume.assumeTrue("This driver doesn't seem to have connectivity to: " + url, false);
        }
    }

    public <T> void waitForCondition(ExpectedCondition<T> condition) {
        waitForCondition(condition, Configuration.current().defaultTimeOut);
    }

    public <T> void waitForCondition(ExpectedCondition<T> condition, int timeoutInSeconds) {
        new WebDriverWait(getDriver(), timeoutInSeconds).until(condition);
    }

    public void driverWaitElementPresent(By pBy) {
        driverWaitElementPresent(pBy, Configuration.current().defaultTimeOut);
    }

    public void driverWaitElementPresent(By pBy, int timeoutInSeconds) {
        new WebDriverWait(getDriver(), timeoutInSeconds).until(ExpectedConditions.presenceOfElementLocated(pBy));
    }

    public FluentWait<WebDriverType> driverFluentWait() {
        return driverFluentWait(Configuration.current().defaultTimeOut);
    }

    public FluentWait<WebDriverType> driverFluentWait(int timeoutInSeconds) {
        return Try.toGet(() -> {
            FluentWait<WebDriverType> fluentWait = new FluentWait<>(getDriver()).withTimeout(timeoutInSeconds, TimeUnit.SECONDS);
            fluentWait.ignoring(WebDriverException.class, ClassCastException.class);
            fluentWait.ignoring(NoSuchElementException.class);
            return fluentWait;
        })
            .onError((t, e) -> logger.warn("driverFluentWait Failed attempt : " + t.currentAttempt() + "/n" + e))
            .onLastError((t, e) -> {
                throw new WebDriverException("driverFluentWait failed after ten attempts");
            })
            .nTimes(10);
    }

    @Override
    public String toString() {
        return getDescription();
    }

    public DesiredCapabilities getCapability() {
        return capabilities;
    }

    public static Predicate<WebDriverWrapper<?>> filter(FilterDrivers filterDrivers) {
        return filter(filterDrivers.filter())
            .and(filter(filterDrivers.platforms()))
            .and(filter(filterDrivers.types()))
            .and(filter(filterDrivers.browsers()))
            .and(filter(filterDrivers.capabilities()));
    }

    public static Predicate<WebDriverWrapper<?>> filter(Class<? extends Predicate<WebDriverWrapper<?>>> filterClass) {
        try {
            return filterClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Predicate<WebDriverWrapper<?>> filter(Platform... values) {
        Set<Platform> platforms = Sets.newHashSet(values);
        return driverWrapper -> {
            return platforms.isEmpty() || platforms.contains(driverWrapper.getCapability().getPlatform());
        };
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static Predicate<WebDriverWrapper<?>> filter(Class<? extends WebDriver>... values) {
        Set<Class<? extends WebDriver>> types = Sets.newHashSet(values);
        return driverWrapper -> {
            Class<?> expectedType = TypeResolver.resolveRawArguments(Function.class, driverWrapper.function.getClass())[1];
            return types.isEmpty() || types.stream().map(type -> ClassUtils.isAssignable(expectedType, type)).findFirst().orElse(false);
        };
    }

    public static Predicate<WebDriverWrapper<?>> filter(Browser... values) {
        Set<String> browsers = Sets.newHashSet(values).stream().map(value -> value.toString().toLowerCase()).collect(Collectors.toSet());
        return driverWrapper -> browsers.isEmpty() || browsers.contains(driverWrapper.getCapability().getBrowserName());
    }

    public static Predicate<WebDriverWrapper<?>> filter(Capability... values) {
        Set<Capability> capabilities = Sets.newHashSet(values);
        return driverWrapper -> {
            return capabilities.isEmpty() || capabilities.stream().allMatch(capability -> {
                String currCap = (String) driverWrapper.getCapability().getCapability(capability.name());
                if (currCap == null) {
                    currCap = "";
                }
                return currCap.equalsIgnoreCase(capability.value());
            });
        };
    }

    public static Predicate<WebDriverWrapper<?>> filterFromSystemProperties() {
        Predicate<WebDriverWrapper<?>> filter = any -> true;

        if (System.getProperty(CapabilityType.BROWSER_NAME) != null) {
            Set<String> browsers = new HashSet<>(Splitter.on(',').trimResults().splitToList(System.getProperty(CapabilityType.BROWSER_NAME)));
            filter = filter.and(driverWrapper -> browsers.contains(driverWrapper.getCapability().getBrowserName()));
        }
        if (System.getProperty(CapabilityType.PLATFORM) != null) {
            Set<Platform> platforms = Splitter.on(',').trimResults().splitToList(System.getProperty(CapabilityType.PLATFORM))
                .stream().map(platform -> Platform.valueOf(platform)).collect(Collectors.toSet());
            filter = filter.and(driverWrapper -> platforms.contains(driverWrapper.getCapability().getPlatform()));
        }
        return filter;
    }

}
