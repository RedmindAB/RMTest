package cucumber.runtime.formatter;

import gherkin.formatter.model.*;

/**
 * @author Jeremy Comte
 */
public class NonRepeatingJSONFormatter extends CucumberJSONFormatter {

    private boolean backgroundHasBeenPrinted;
    private boolean printing = true;

    public NonRepeatingJSONFormatter(Appendable out) {
        super(out);
    }

    @Override
    public void feature(Feature feature) {
        super.feature(feature);
        backgroundHasBeenPrinted = false;
    }

    @Override
    public void background(Background background) {
        if (backgroundHasBeenPrinted) {
            printing = false;
        } else {
            super.background(background);
        }
        backgroundHasBeenPrinted = true;
    }

    @Override
    public void scenario(Scenario scenario) {
        printing = true;
        super.scenario(scenario);
    }

    @Override
    public void step(Step step) {
        if (printing) {
            super.step(step);
        }
    }

    @Override
    public void match(Match match) {
        if (printing) {
            super.match(match);
        }
    }

    @Override
    public void result(Result result) {
        if (printing) {
            super.result(result);
        }
    }

}
