package se.redmind.rmtest.cucumber;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.*;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.CucumberOptions;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import se.redmind.rmtest.runners.ParameterizedCucumberRunnerFactory;
import se.redmind.rmtest.runners.WebDriverRunner;
import se.redmind.utils.LogBackUtil;
import se.redmind.utils.Methods;
import se.redmind.utils.ReflectionsUtils;

/**
 * @author Jeremy Comte
 */
public class CucumberRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(CucumberRunner.class);

    public static void main(String[] args) throws Throwable {
        LogBackUtil.install();
        Options options = new Options();
        options.addOption(Option.builder("p").longOpt("print-step-definitions").required(false).desc("print all the known step definitions").build());
        options.addOption(Option.builder("D").argName("property=value").numberOfArgs(2).valueSeparator().desc("use value for given property").build());
        options.addOption(Option.builder("h").longOpt("help").required(false).desc("this help").build());

        CommandLineParser parser = new DefaultParser();
        CommandLine command = null;

        try {
            command = parser.parse(options, args);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            printHelp(options);
            System.exit(-1);
        }

        if (command.hasOption('h')) {
            printHelp(options);
        } else if (command.hasOption("p")) {
            Set<Method> cucumberMethods = Stream.of(Given.class, Then.class, And.class, When.class)
                .map(annotation -> ReflectionsUtils.current().getMethodsAnnotatedWith(annotation))
                .flatMap(Collection::stream).collect(Collectors.toSet());
            LOGGER.info("known step definitions:");
            cucumberMethods.forEach(method -> {
                String pattern = Stream.of(Given.class, Then.class, And.class, When.class)
                    .filter(method::isAnnotationPresent)
                    .map(annotation -> (String) Methods.invoke(method.getAnnotation(annotation), "value")).findFirst().get();
                LOGGER.info(pattern);
            });
        } else {
            new CucumberRunner().run();
        }
    }

    private static void printHelp(Options options) {
        new HelpFormatter().printHelp(100, "java -jar rmtest-cucumber-standalone.jar", "", options, "", true);
    }

    public void run() throws Throwable {
        Result result = new JUnitCore().run(Request.runner(new WebDriverRunner(Test.class)));
        if (result.getFailureCount() > 0) {
            LOGGER.error(new PrintableResult(result.getFailures()).toString());
        }
        LOGGER.info(String.format("Tests run: %d, Failures: %d, Skipped: %d, Time elapsed: %.4f sec",
            result.getRunCount(), result.getFailureCount(), result.getIgnoreCount(), result.getRunTime() / 1_000.0));
    }

    @RunWith(WebDriverRunner.class)
    @Parameterized.UseParametersRunnerFactory(ParameterizedCucumberRunnerFactory.class)
    @CucumberOptions(glue = "se.redmind.rmtest.cucumber.web", features = ".", plugin = "pretty")
    public static class Test {

    }

}
