package se.redmind.rmtest.cucumber.parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import cucumber.api.CucumberOptions;
import cucumber.runtime.ParameterizableRuntime.CompositionType;
import se.redmind.rmtest.runners.ParameterizedCucumberRunnerFactory;

/**
 * @author Jeremy Comte
 */
@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(ParameterizedCucumberRunnerFactory.class)
@CucumberOptions(plugin = {"pretty", "json:target/ParameterizedStepsFullTest-json-report.json", "html:target/ParameterizedStepsFullTest-hmtl-report"}, tags = {"@tag1,@tag2,@tag3,@ignore"})
public class ParameterizedStepsFullTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(CompositionType.full).stream()
            .peek(value -> System.setProperty("cucumber.compositionType", value.name()))
            .map(value -> new Object[]{"compositionType: " + value}).collect(Collectors.toList());
    }
}
