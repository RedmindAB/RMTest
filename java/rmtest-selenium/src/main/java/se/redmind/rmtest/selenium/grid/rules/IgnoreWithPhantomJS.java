package se.redmind.rmtest.selenium.grid.rules;

import se.redmind.rmtest.config.Configuration;
import se.redmind.rmtest.config.PhantomJSConfiguration;

public class IgnoreWithPhantomJS implements ConditionalRule.IgnoreCondition {

    @Override
    public boolean isSatisfied() {
        return Configuration.current().drivers.stream().anyMatch(driver -> driver instanceof PhantomJSConfiguration);
    }

}
