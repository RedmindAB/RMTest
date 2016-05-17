package se.redmind.rmtest.runners;

import java.util.Arrays;
import java.util.List;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * @author Johan Grimlund
 */
public class ParallelizedSuite extends Suite implements Parallelizable {

    public ParallelizedSuite(final Class<?> klass) throws InitializationError {
        super(klass, new AllDefaultPossibilitiesBuilder(true) {
            @Override
            public Runner runnerForClass(Class<?> testClass) throws Throwable {
                List<RunnerBuilder> builders = Arrays.asList(
                    new RunnerBuilder() {
                        @Override
                        public Runner runnerForClass(Class<?> testClass) throws Throwable {
                            if (testClass.isAnnotationPresent(Parallelize.class) && !testClass.isAnnotationPresent(RunWith.class)) {
                                return new ParallelizedRunner(testClass);
                            }
                            return null;
                        }
                    },
                    ignoredBuilder(),
                    annotatedBuilder(),
                    suiteMethodBuilder(),
                    junit3Builder(),
                    junit4Builder());
                for (RunnerBuilder each : builders) {
                    Runner runner = each.safeRunnerForClass(testClass);
                    if (runner != null) {
                        return runner;
                    }
                }
                return null;
            }
        });
        parallelize();
    }

}
