package se.redmind.rmtest.runners;

import java.io.IOException;

import org.junit.runners.model.InitializationError;
import org.junit.runners.parameterized.ParametersRunnerFactory;
import org.junit.runners.parameterized.TestWithParameters;

import cucumber.api.junit.Cucumber;

/**
 * This class is used to create the parameterized cucumber sub runner.
 *
 * @author Jeremy Comte
 */
public class ParameterizedCucumberRunnerFactory implements ParametersRunnerFactory {

    @Override
    public Cucumber createRunnerForTestWithParameters(TestWithParameters test) throws InitializationError {
        try {
            return new Cucumber(test.getTestClass().getJavaClass(), test.getName(), test.getParameters().toArray());
        } catch (IOException ex) {
            throw new InitializationError(ex);
        }
    }

}
