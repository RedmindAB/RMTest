package cucumber.api.cli;

import java.io.IOException;
import java.util.ArrayList;

import cucumber.runtime.ClassFinder;
import cucumber.runtime.ParameterizableRuntime;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import se.redmind.rmtest.config.Configuration;

import static java.util.Arrays.asList;

import se.redmind.rmtest.cucumber.utils.Tags;

public class Main {

    public static void main(String[] argv) throws Throwable {
        byte exitstatus = run(argv, Thread.currentThread().getContextClassLoader());
        System.exit(exitstatus);
    }

    /**
     * Launches the Cucumber-JVM command line.
     *
     * @param argv runtime options. See details in the {@code cucumber.api.cli.Usage.txt} resource.
     * @param classLoader classloader used to load the runtime
     * @return 0 if execution was successful, 1 if it was not (test failures)
     * @throws IOException if resources couldn't be loaded during the run.
     */
    public static byte run(String[] argv, ClassLoader classLoader) throws IOException {
        byte exitstatus = 0;
        for (Object[] parameters : Configuration.current().createWrappersParameters()) {
            if (exitstatus != 0) {
                break;
            }
            RuntimeOptions runtimeOptions = new RuntimeOptions(new ArrayList<>(asList(argv)));
            ResourceLoader resourceLoader = new MultiLoader(classLoader);
            ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
            Runtime runtime = new ParameterizableRuntime(resourceLoader, classFinder, classLoader, runtimeOptions, parameters[0].toString(), parameters);
            runtime.run();
            exitstatus = runtime.exitStatus();
        }
        return exitstatus;
    }
}
