package cucumber.api.junit;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import cucumber.runtime.junit.*;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.Result;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import cucumber.api.CucumberOptions;
import cucumber.runtime.*;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.*;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import se.redmind.utils.Fields;

/**
 * <p>
 * Classes annotated with {@code @RunWith(Cucumber.class)} will run a Cucumber Feature. The class should be empty without any fields or methods.
 * </p>
 * <p>
 * Cucumber will look for a {@code .feature} file on the classpath, using the same resource path as the annotated class ({@code .class} substituted by
 * {@code .feature}).
 * </p>
 * Additional hints can be given to Cucumber by annotating the class with {@link CucumberOptions}.
 * <p>
 * this class has been extended in place because we needed to be able to forward the parameters in the parameterized runtime.
 *
 * @see CucumberOptions
 */
public class Cucumber extends ParentRunner<FeatureRunner> {

    private final JUnitReporter jUnitReporter;
    private final List<FeatureRunner> children = new ArrayList<>();
    private final ParameterizableRuntime runtime;
    private final String name;
    private final boolean reportRealClassNames;
    private final boolean hideStepsInReports;
    // do not remove this field, it is read through reflection
    private final Object[] parameters;

    /**
     * Constructor called by JUnit.
     *
     * @param clazz the class with the @RunWith annotation.
     * @throws java.io.IOException                         if there is a problem
     * @throws org.junit.runners.model.InitializationError if there is another problem
     */
    public Cucumber(Class clazz) throws InitializationError, IOException {
        this(clazz, null, new Object[0]);
    }

    public Cucumber(Class clazz, String name, Object... parameters) throws InitializationError, IOException {
        super(clazz);
        this.name = name;
        this.parameters = parameters;

        reportRealClassNames = "true".equals(System.getProperty("reportRealClassNames"));
        hideStepsInReports = "true".equals(System.getProperty("hideStepsInReports"));

        ClassLoader classLoader = clazz.getClassLoader();
        Assertions.assertNoCucumberAnnotatedMethods(clazz);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        runtime = new ParameterizableRuntime(resourceLoader, new ResourceLoaderClassFinder(resourceLoader, classLoader), classLoader, runtimeOptions, name, parameters);

        final List<CucumberFeature> cucumberFeatures = runtime.cucumberFeatures();
        Reporter reporter = runtimeOptions.reporter(classLoader);
        jUnitReporter = new JUnitReporter(reporter, runtimeOptions.formatter(classLoader), runtimeOptions.isStrict()) {

            public void result(Result result) {
                if (hideStepsInReports) {
                    Throwable error = result.getError();
                    if (error != null) {
                        EachTestNotifier executionUnitNotifier = Fields.getSafeValue(this, "executionUnitNotifier");
                        if (executionUnitNotifier != null) {
                            executionUnitNotifier.addFailure(error);
                        }
                    }
                    reporter.result(result);
                } else {
                    super.result(result);
                }
            }

        };
        addChildren(cucumberFeatures);
    }

    @Override
    public List<FeatureRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(FeatureRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(FeatureRunner child, RunNotifier notifier) {
        child.run(notifier);
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
        jUnitReporter.done();
        jUnitReporter.close();
        runtime.printSummary();
    }

    private void addChildren(List<CucumberFeature> cucumberFeatures) throws InitializationError {
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            if (!cucumberFeature.getFeatureElements().isEmpty()) {
                FeatureRunner featureRunner = new FeatureRunner(cucumberFeature, runtime, jUnitReporter);
                appendParameterizedName(featureRunner);
                children.add(featureRunner);
            }
        }
    }

    private void appendParameterizedName(FeatureRunner featureRunner) throws InitializationError {
        List<ParentRunner<?>> runners = Fields.getSafeValue(featureRunner, "children");
        CucumberFeature cucumberFeature = Fields.getSafeValue(featureRunner, "cucumberFeature");
        String featureName = ((String) Fields.getSafeValue(cucumberFeature, "path")).replaceFirst(".feature$", "").replaceAll("/", ".");
        Map<String, StepDefinition> stepDefinitionsByPattern = Fields.getSafeValue(runtime.getGlue(), "stepDefinitionsByPattern");
        for (int i = 0; i < runners.size(); i++) {
            ParentRunner<?> runner = runners.get(i);
            if (runner instanceof ExecutionUnitRunner) {
                runner = replaceExecutionRunner(featureName, stepDefinitionsByPattern, (ExecutionUnitRunner) runner);
            } else if (runner instanceof ScenarioOutlineRunner) {
                runner = replaceScenarioOutlineRunner(featureName, stepDefinitionsByPattern, (ScenarioOutlineRunner) runner);
            }
            runners.set(i, runner);
        }
    }


    private ExecutionUnitRunner replaceExecutionRunner(String featureName, Map<String, StepDefinition> stepDefinitionsByPattern, ExecutionUnitRunner runner) throws InitializationError {
        CucumberScenario cucumberScenario = Fields.getSafeValue(runner, "cucumberScenario");
        Scenario scenario = Fields.getSafeValue(cucumberScenario, "scenario");
        return new ExecutionUnitRunner(runtime, cucumberScenario, jUnitReporter) {
            @Override
            public Description getDescription() {
                Description description = super.getDescription();
                if (reportRealClassNames && !description.getClassName().equals(featureName)) {
                    Fields.set(description, "fDisplayName", featureName + ":" + scenario.getLine() + (name != null ? name : "")
                        + "(" + Cucumber.this.getTestClass().getJavaClass().getName() + ")");
                }
                return description;
            }

            @Override
            protected Description describeChild(Step step) {
                Description description = super.describeChild(step);
                if (!description.getMethodName().contains("#" + step.getLine())) {
                    Method method = findMatchingMethod(step);
                    if (reportRealClassNames && method != null) {
                        Fields.set(description, "fDisplayName", method.getName() + "@" + featureName + "#" + step.getLine() + (name != null ? name : "")
                            + "(" + method.getDeclaringClass().getName() + ")");
                    } else {
                        Fields.set(description, "fDisplayName", description.getMethodName() + "#" + step.getLine() + (name != null ? name : "")
                            + "(" + description.getClassName() + ")");
                    }
                }
                return description;
            }

            private Method findMatchingMethod(Step step) {
                Method method = null;
                for (Map.Entry<String, StepDefinition> entry : stepDefinitionsByPattern.entrySet()) {
                    if (step.getName().matches(entry.getKey())) {
                        method = Fields.getSafeValue(entry.getValue(), "method");
                        break;
                    }
                }
                return method;
            }
        };
    }

    private ScenarioOutlineRunner replaceScenarioOutlineRunner(String featureName, Map<String, StepDefinition> stepDefinitionsByPattern, ScenarioOutlineRunner runner) throws InitializationError {
        CucumberScenarioOutline cucumberScenarioOutline = Fields.getSafeValue(runner, "cucumberScenarioOutline");
        ScenarioOutlineRunner scenarioOutlineRunner = new ScenarioOutlineRunner(runtime, cucumberScenarioOutline, jUnitReporter) {

            @Override
            public Description getDescription() {
                if (reportRealClassNames) {
                    Description description = Fields.getSafeValue(this, "description");
                    if (description == null) {
                        description = Description.createSuiteDescription(getName(), cucumberScenarioOutline.getGherkinModel());
                        List<Runner> children = getChildren();
                        for (int i = 0; i < children.size(); i++) {
                            Runner child = children.get(i);
                            Description childDescription = describeChild(child);
                            CucumberExamples cucumberExamples = cucumberScenarioOutline.getCucumberExamplesList().get(i);
                            for (int j = 0; j < childDescription.getChildren().size(); j++) {
                                Description exampleDescription = childDescription.getChildren().get(j);
                                ExamplesTableRow examplesTableRow = cucumberExamples.getExamples().getRows().get(j);
                                Fields.set(exampleDescription, "fDisplayName", featureName + ":" + examplesTableRow.getLine() + (name != null ? name : "")
                                    + "(" + Cucumber.this.getTestClass().getJavaClass().getName() + ")");
                            }
                            description.addChild(childDescription);
                        }
                        Fields.set(this, "description", description);
                    }
                }
                return super.getDescription();
            }
        };

        List<ExamplesRunner> children = Fields.getSafeValue(scenarioOutlineRunner, "runners");
        for (ExamplesRunner child : children) {
            List<ExecutionUnitRunner> currentRunners = Fields.getSafeValue(child, "runners");
            List<ExecutionUnitRunner> runners = new ArrayList<>();
            for (ExecutionUnitRunner currentRunner : currentRunners) {
                runners.add(replaceExecutionRunner(featureName, stepDefinitionsByPattern, currentRunner));
            }
            Fields.set(child, "runners", runners);
        }

        return scenarioOutlineRunner;
    }

}
