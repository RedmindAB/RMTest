package cucumber.runtime.formatter;

import gherkin.formatter.model.*;
import java.net.URL;

/**
 * @author Jeremy Comte
 */
public class NonRepeatingHTMLFormatter extends HTMLFormatter {

    private boolean backgroundHasBeenPrinted;
    private boolean printing = true;

    public NonRepeatingHTMLFormatter(URL htmlReportDir) {
        super(htmlReportDir);
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

}
