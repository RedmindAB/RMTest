package se.redmind.rmtest.runners;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.redmind.rmtest.WebDriverWrapper;
import se.redmind.rmtest.config.Configuration;
import se.redmind.rmtest.config.GridConfiguration;
import se.redmind.rmtest.selenium.livestream.LiveStreamListener;
import se.redmind.utils.Annotations;
import se.redmind.utils.Fields;

/**
 * Parameterized runner that will pick up all the drivers configuration and inject our test classes automatically
 *
 * @author Jeremy Comte
 */
public class WebDriverRunner extends Parameterized implements Parallelizable {

    protected static final Logger LOGGER = LoggerFactory.getLogger(WebDriverRunner.class);
    private LiveStreamListener liveStreamListener;
    private final WebDriverRunnerOptions options;

    public WebDriverRunner(Class<?> klass) throws Throwable {
        super(klass);
        options = Annotations.collectAndCombine(WebDriverRunnerOptions.class, klass);
    }

    @Override
    public void run(RunNotifier notifier) {
        parallelize(options.parallelize());
        if (Configuration.current().drivers.stream().anyMatch(driver -> driver instanceof GridConfiguration && driver.as(GridConfiguration.class).enableLiveStream)) {
            liveStreamListener = new LiveStreamListener();
            notifier.addListener(liveStreamListener);
        }
        notifier.fireTestRunStarted(getDescription());
        super.run(notifier);
    }

    @Override
    protected void runChild(Runner runner, RunNotifier notifier) {
        Optional<WebDriverWrapper<?>> driverWrapper = getCurrentDriverWrapper(runner);
        if (driverWrapper.isPresent()) {
            if (Configuration.current().reuseDriverBetweenTests && options.reuseDriver()) {
                driverWrapper.get().setReuseDriverBetweenTests(true);
            }

            if (runner instanceof BlockJUnit4ClassRunner) {
                try {
                    ((ParentRunner<?>) runner).filter(new Filter() {
                        @Override
                        public boolean shouldRun(Description description) {
                            List<FilterDrivers> filterDrivers = Annotations.collect(FilterDrivers.class, description.getClass());
                            if (!filterDrivers.isEmpty()) {
                                if (!WebDriverWrapper.filter(Annotations.combine(filterDrivers)).test(driverWrapper.get())) {
                                    notifier.fireTestIgnored(description);
                                    return false;
                                }
                            }
                            return true;
                        }

                        @Override
                        public String describe() {
                            return "FilterDrivers";
                        }
                    });
                } catch (NoTestsRemainException ex) {
                    return;
                }

                if (!driverWrapper.get().reuseDriverBetweenTests()) {
                    notifier.addListener(new RunListener() {

                        @Override
                        public void testFinished(Description description) throws Exception {
                            driverWrapper.get().stopDriver();
                        }

                    });
                }
            }
        }

        if (liveStreamListener != null) {
            notifier.addListener(liveStreamListener.getSubListener());
        }

        super.runChild(runner, notifier);
        if (driverWrapper.isPresent() && Configuration.current().autoCloseDrivers) {
            driverWrapper.get().stopDriver();
        }
    }

    protected Optional<WebDriverWrapper<?>> getCurrentDriverWrapper(Runner runner) {
        Object[] parameters = Fields.getValue(runner, "parameters");
        for (Object parameter : parameters) {
            if (parameter instanceof WebDriverWrapper) {
                return Optional.of((WebDriverWrapper<?>) parameter);
            }
        }
        return Optional.empty();
    }

    @Override
    protected TestClass createTestClass(Class<?> testClass) {
        // The original Parameterized code requires the method annotated with @Parameters to be static and located on our test class.
        // We want to be able to inject the WebDrivers defined in our config file directly from this WebDriverRunner instance.
        return new TestClass(testClass) {

            private final TestClass runnerAsTestClass = new TestClass(WebDriverRunner.this.getClass()) {
                @Override
                protected void scanAnnotatedMembers(Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations,
                                                    Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations) {
                    try {
                        addToAnnotationLists(new FrameworkMethod(WebDriverRunner.this.getClass().getMethod("getDriversAsParameters")) {

                            @Override
                            protected int getModifiers() {
                                // Pretend to be a static method
                                return super.getModifiers() | Modifier.STATIC;
                            }

                            @Override
                            public Object invokeExplosively(Object target, Object... params) throws Throwable {
                                // invoke the method in this instead
                                return super.invokeExplosively(WebDriverRunner.this, params);
                            }

                        }, methodsForAnnotations);
                    } catch (NoSuchMethodException | SecurityException ex) {
                        throw new RuntimeException(ex);
                    }
                    super.scanAnnotatedMembers(methodsForAnnotations, fieldsForAnnotations);
                }
            };

            @Override
            public List<FrameworkMethod> getAnnotatedMethods(Class<? extends Annotation> annotationClass) {
                List<FrameworkMethod> methods = super.getAnnotatedMethods(annotationClass);
                if (methods.isEmpty()) {
                    // offer the local method only if the current test class doesn't have any method annotated with @Parameters
                    return runnerAsTestClass.getAnnotatedMethods(annotationClass);
                }
                return methods;
            }
        };
    }

    @Parameterized.Parameters(name = "{0}")
    public Collection<Object[]> getDriversAsParameters() {
        Collection<Object[]> drivers;
        List<FilterDrivers> filterDrivers = Annotations.collect(FilterDrivers.class, getTestClass().getJavaClass());
        if (!filterDrivers.isEmpty()) {
            FilterDrivers combined = Annotations.combine(filterDrivers);
            drivers = Configuration.current().createWrappersParameters(combined);
            if (drivers.isEmpty()) {
                LOGGER.warn("we didn't find any driver matching our filter " + combined);
            }
        } else {
            drivers = Configuration.current().createWrappersParameters();
            if (drivers.isEmpty()) {
                LOGGER.warn("we didn't find any driver");
            }
        }

        return drivers;
    }
}
