package gherkin.formatter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.runtime.StepDefinitionMatch;
import gherkin.formatter.model.*;
import se.redmind.utils.Fields;

/**
 * @author Jeremy Comte
 */
public class Log4Cucumber implements Reporter, Formatter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void before(Match match, Result result) {
    }

    @Override
    public void result(Result result) {
    }

    @Override
    public void after(Match match, Result result) {
    }

    @Override
    public void match(Match match) {
        if (match instanceof StepDefinitionMatch) {
            Step step = Fields.getSafeValue(match, "step");
            logger.debug("    " + step.getKeyword() + step.getName());
            if (step.getRows() != null) {
                Row header = step.getRows().get(0);
                int[] cellLengths = new int[header.getCells().size()];
                for (int i = 1; i < step.getRows().size(); i++) {
                    Row row = step.getRows().get(i);
                    for (int j = 0; j < cellLengths.length; j++) {
                        cellLengths[j] = Math.max(cellLengths[j], row.getCells().get(j).length());
                    }
                }

                for (int i = 1; i < step.getRows().size(); i++) {
                    Row row = step.getRows().get(i);
                    StringBuilder stringBuilder = new StringBuilder("      | ");
                    for (int j = 0; j < header.getCells().size(); j++) {
                        String content = row.getCells().get(j);
                        stringBuilder.append(header.getCells().get(j)).append(": ").append(content);
                        pad(stringBuilder, stringBuilder.length() + (cellLengths[j] - content.length()));
                        stringBuilder.append("| ");
                    }
                    stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
                    logger.debug(stringBuilder.toString());
                }
            }
        }
    }

    private void pad(StringBuilder stringBuilder, int until) {
        while (stringBuilder.length() <= until) {
            stringBuilder.append(" ");
        }
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
    }

    @Override
    public void write(String text) {
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    }

    @Override
    public void uri(String uri) {
    }

    @Override
    public void feature(Feature feature) {
        logger.debug("Feature: " + feature.getName());
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
    }

    @Override
    public void examples(Examples examples) {
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
    }

    @Override
    public void background(Background background) {
        logger.debug("  Background: " + background.getName());
    }

    @Override
    public void scenario(Scenario scenario) {
        logger.debug("  Scenario: " + scenario.getName());
    }

    @Override
    public void step(Step step) {
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
    }

    @Override
    public void done() {
    }

    @Override
    public void close() {
    }

    @Override
    public void eof() {
    }

}
