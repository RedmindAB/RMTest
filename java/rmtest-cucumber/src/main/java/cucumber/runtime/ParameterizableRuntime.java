package cucumber.runtime;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.StepDefinitionReporter;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java.ParameterizedJavaStepDefinition;
import cucumber.runtime.java.picocontainer.PicoFactory;
import cucumber.runtime.model.*;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;
import se.redmind.rmtest.cucumber.utils.Tags;
import se.redmind.utils.Fields;
import se.redmind.utils.Methods;

/**
 * @author Jeremy Comte
 */
public class ParameterizableRuntime extends Runtime {

    public static enum CompositionType {

        replace, full, quiet
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RuntimeOptions runtimeOptions;
    private final ClassLoader classLoader;
    private final ResourceLoader resourceLoader;
    private final String name;
    private final Object[] parameters;

    private PicoFactory picoFactory;

    public ParameterizableRuntime(ResourceLoader resourceLoader, ClassFinder classFinder, ClassLoader classLoader, RuntimeOptions runtimeOptions) {
        this(resourceLoader, classFinder, classLoader, runtimeOptions, null, new Object[0]);
    }

    public ParameterizableRuntime(ResourceLoader resourceLoader, ClassFinder classFinder, ClassLoader classLoader, RuntimeOptions runtimeOptions, String name, Object[] parameters) {
        super(resourceLoader, classFinder, classLoader, runtimeOptions);
        this.runtimeOptions = runtimeOptions;
        this.classLoader = classLoader;
        this.resourceLoader = resourceLoader;
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    public void run() throws IOException {
        // Make sure all features parse before initialising any reporters/formatters
        List<CucumberFeature> features = cucumberFeatures();

        try (Formatter formatter = runtimeOptions.formatter(classLoader)) {
            Reporter reporter = runtimeOptions.reporter(classLoader);
            StepDefinitionReporter stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader);
            getGlue().reportStepDefinitions(stepDefinitionReporter);
            features.forEach(cucumberFeature -> cucumberFeature.run(formatter, reporter, this));
            formatter.done();
        }
        printSummary();
    }

    public List<CucumberFeature> cucumberFeatures() {
        // default cucumber overrides the filters given in the @CucumberOptions annotation
        // using the cucumber.filters System.property, one can extend the filters instead of overriding it.
        List<String> extraFilters = Shellwords.parse(System.getProperty("cucumber.filters", ""));
        for (int i = 0; i < extraFilters.size(); i += 2) {
            String type = extraFilters.get(i).trim();
            switch (type) {
                case "--tags":
                    runtimeOptions.getFilters().add(extraFilters.get(i + 1).trim());
                    break;
                case "--name":
                    runtimeOptions.getFilters().add(Pattern.compile(extraFilters.get(i + 1).trim()));
                    break;
            }
        }

        // if we work with tags we want to add the @parameterized and ~@ignore filters
        boolean hasTags = false;
        Object filter = null;
        for (int i = 0; i < runtimeOptions.getFilters().size(); i++) {
            filter = runtimeOptions.getFilters().get(i);
            if (filter instanceof String && ((String) filter).contains("@")) {
                hasTags = true;
                runtimeOptions.getFilters().set(i, ((String) filter) + "," + Tags.PARAMETERIZED);
            }
        }

        List<CucumberFeature> cucumberFeatures = new ArrayList<>();
        if (filter == null || hasTags) {
            runtimeOptions.getFilters().add("~" + Tags.IGNORE);
        } else {
            // otherwise we will look for the @parameterized scenarios and add them
            RuntimeOptions parameterizedScenarioRuntimeOptions = new RuntimeOptions("");
            parameterizedScenarioRuntimeOptions.getFilters().add(Tags.PARAMETERIZED);
            parameterizedScenarioRuntimeOptions.getFilters().add("~" + Tags.IGNORE);
            parameterizedScenarioRuntimeOptions.getFeaturePaths().add("classpath:");
            cucumberFeatures.addAll(parameterizedScenarioRuntimeOptions.cucumberFeatures(resourceLoader));
        }

        cucumberFeatures.addAll(runtimeOptions.cucumberFeatures(resourceLoader));

        // 1. Get the children from the parent class, intercept any parameterized scenario and instantiate their factories
        Map<Pattern, ParameterizedJavaStepDefinition.Factory> parameterizedScenarios = getParameterizedScenarios(cucumberFeatures);

        // 2. Iterate over all the normal steps, and if the scenario is not quiet, rewrite and add the parameterized steps as normal steps.
        if (!cucumberFeatures.isEmpty() && !parameterizedScenarios.isEmpty()) {
            inject(parameterizedScenarios, cucumberFeatures);
        }
        return cucumberFeatures;
    }

    @Override
    public void buildBackendWorlds(Reporter reporter, Set<Tag> tags, Scenario gherkinScenario) {
        for (Object parameter : parameters) {
            picoFactory().addInstance(parameter);
        }
        super.buildBackendWorlds(reporter, tags, gherkinScenario);
    }

    public Map<Pattern, ParameterizedJavaStepDefinition.Factory> getParameterizedScenarios(List<CucumberFeature> features) {
        Map<Pattern, ParameterizedJavaStepDefinition.Factory> parameterizedScenarios = new LinkedHashMap<>();
        for (int i = 0; i < features.size(); i++) {
            CucumberFeature feature = features.get(i);
            List<CucumberTagStatement> statements = feature.getFeatureElements();
            for (int j = 0; j < statements.size(); j++) {
                CucumberTagStatement statement = statements.get(j);
                if (Tags.isParameterized(statement)) {
                    ParameterizedJavaStepDefinition.Factory stepFactory = ParameterizedJavaStepDefinition.from(statement, this);
                    parameterizedScenarios.put(stepFactory.pattern(), stepFactory);
                    if (stepFactory.parameters().length == 0) {
                        stepFactory.addQuietSubStepsToGlue();
                    }
                    statements.remove(j--);
                } else if (name != null) {
                    TagStatement tagStatement = statement.getGherkinModel();
                    Fields.set(tagStatement, "name", tagStatement.getName() + " " + name);
                }
            }
            if (statements.isEmpty()) {
                features.remove(i--);
            }
        }

        if (!features.isEmpty() && !parameterizedScenarios.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            int maxLength = parameterizedScenarios.values().stream().map(f -> f.statement().getVisualName().length()).max(Integer::compareTo).orElse(0);
            parameterizedScenarios.values().forEach(factory -> {
                CucumberFeature cucumberFeature = Fields.getValue(factory.statement(), "cucumberFeature");
                String path = Fields.getValue(cucumberFeature, "path");
                String visualName = factory.statement().getVisualName().replaceAll("Scenario:", "");
                stringBuilder.append("\n  ").append(visualName);
                for (int i = 0; i < maxLength - visualName.length() - 5; i++) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append("# ").append(path).append(":").append(factory.statement().getGherkinModel().getLine());
            });
            logger.info("\nregistering parameterized scenarios:" + stringBuilder.toString() + "\n");
        }
        return parameterizedScenarios;
    }

    public void inject(Map<Pattern, ParameterizedJavaStepDefinition.Factory> parameterizedScenarios, List<CucumberFeature> features) throws RuntimeException {
        CompositionType compositionType = CompositionType.valueOf(System.getProperty("cucumber.compositionType", CompositionType.replace.name()));
        if (compositionType == CompositionType.full) {
            picoFactory().addInstance(this);
            getGlue().addStepDefinition(new ParameterizedJavaStepDefinition(Methods.findMethod(this.getClass(), "endOfParameterizedScenario"), Pattern.compile("}"), 0, picoFactory()));
        }
        features.forEach(feature -> {
            List<StepContainer> stepContainers = new ArrayList<>(feature.getFeatureElements());

            CucumberBackground cucumberBackground = Fields.getValue(feature, "cucumberBackground");
            if (cucumberBackground != null) {
                stepContainers.add(cucumberBackground);
            }

            parameterizedScenarios.values().forEach(scenario -> stepContainers.add(scenario.statement()));

            int modifiedSteps;
            // we need to keep trying as long as we find new parameterizable steps in order to support composite sub scenarios
            do {
                modifiedSteps = 0;
                for (StepContainer stepContainer : stepContainers) {
                    for (int i = 0; i < stepContainer.getSteps().size(); i++) {
                        Step step = stepContainer.getSteps().get(i);

                        if (step instanceof ParameterizedStep) {
                            if (((ParameterizedStep) step).getType() == ParameterizedStep.Type.Start
                                || ((ParameterizedStep) step).getType() == ParameterizedStep.Type.Quiet) {
                                continue;
                            }
                        }
                        String stepName = step.getName();

                        for (Map.Entry<Pattern, ParameterizedJavaStepDefinition.Factory> parameterizedScenario : parameterizedScenarios.entrySet()) {
                            Matcher matcher = parameterizedScenario.getKey().matcher(stepName);
                            if (matcher.matches()) {
                                if (compositionType == CompositionType.quiet) {
                                    stepContainer.getSteps().set(i, ParameterizedStep.asQuiet(step));
                                    if (parameterizedScenario.getValue().parameters().length > 0) {
                                        parameterizedScenario.getValue().addQuietSubStepsToGlue();
                                    }
                                } else {
                                    Function<Step, ParameterizedStep> wrapper;
                                    String[] names = parameterizedScenario.getValue().parameters();
                                    Object[] scenarioParameters = new Object[names.length];
                                    for (int k = 0; k < names.length; k++) {
                                        String value = matcher.group(k + 1);
                                        if (value.startsWith("\"") && value.endsWith("\"")) {
                                            value = value.substring(1, value.length() - 1);
                                        }
                                        scenarioParameters[k] = value;
                                    }
                                    if (compositionType == CompositionType.full) {
                                        parameterizedScenario.getValue().addStartStepToGlue();
                                        stepContainer.getSteps().set(i, ParameterizedStep.startOf(step));
                                        wrapper = parameterizedStep -> ParameterizedStep.asSubStep(parameterizedStep, names, scenarioParameters);
                                    } else {
                                        stepContainer.getSteps().remove(i--);
                                        wrapper = parameterizedStep -> ParameterizedStep.parameterize(parameterizedStep, names, scenarioParameters);
                                    }

                                    List<Step> newSteps = parameterizedScenario.getValue().statement().getSteps().stream()
                                        .map(wrapper)
                                        .collect(Collectors.toList());
                                    stepContainer.getSteps().addAll(i + 1, newSteps);
                                    i += newSteps.size();
                                    if (compositionType == CompositionType.full) {
                                        stepContainer.getSteps().add(++i, ParameterizedStep.endOf(step));
                                    }
                                    modifiedSteps++;
                                }
                                break;
                            }
                        }
                    }
                }
            } while (modifiedSteps > 0);
        });
    }

    /**
     * this method is used as a target for the end of a parameterized scenario
     */
    public void endOfParameterizedScenario() {
    }

    public PicoFactory picoFactory() throws RuntimeException {
        if (picoFactory == null) {
            Collection<? extends Backend> backends = Fields.getValue(this, "backends");
            Optional<JavaBackend> first = backends.stream()
                .filter(backend -> backend instanceof JavaBackend)
                .map(backend -> (JavaBackend) backend)
                .findFirst();
            if (first.isPresent()) {
                picoFactory = Fields.getValue(first.get(), "objectFactory");
            } else {
                throw new RuntimeException("can't find a javaBackend instance");
            }
        }
        return picoFactory;
    }

}
