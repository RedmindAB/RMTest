package cucumber.runtime.model;

import java.util.List;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.Step;
import se.redmind.rmtest.cucumber.utils.Tags;

import static cucumber.runtime.model.ParameterizedStepContainer.replacePlaceHolders;

/**
 * @author Jeremy Comte
 */
public class ParameterizedStep extends Step {

    private static final ThreadLocal<Integer> depth = ThreadLocal.withInitial(() -> 0);

    public enum Type {

        Start, SubStep, Parameterized, Quiet, End
    }

    private final Type type;

    private ParameterizedStep(List<Comment> comments, String keyword, String name, Integer line, List<DataTableRow> rows, DocString docString, Type type) {
        super(comments, keyword, name, line, rows, docString);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String getName() {
        switch (type) {
            case Start:
                return super.getName() + " {";
            case End:
                return "}";
            default:
                return super.getName();
        }
    }

    public String getOriginalName() {
        return super.getName();
    }

    @Override
    public String getKeyword() {
        String keyword;
        switch (type) {
            case Start:
                keyword = addDepthTabs(super.getKeyword(), depth.get());
                depth.set(depth.get() + 1);
                break;
            case End:
                depth.set(depth.get() - 1);
                keyword = addDepthTabs("", depth.get());
                break;
            default:
                keyword = addDepthTabs(super.getKeyword(), depth.get());
        }
        return keyword;
    }

    private String addDepthTabs(String input, int depthValue) {
        StringBuilder output = new StringBuilder(input);
        for (int i = 0; i < depthValue; i++) {
            output.insert(0, "  ");
        }
        return output.toString();
    }

    public String getOriginalKeyword() {
        return super.getKeyword();
    }

    public static ParameterizedStep asQuiet(Step step) {
        return new ParameterizedStep(step.getComments(), step.getKeyword(), step.getName().replaceAll(Tags.QUIET, "").trim(), step.getLine(), step.getRows(), step.getDocString(), Type.Quiet);
    }

    public static ParameterizedStep startOf(Step step) {
        return new ParameterizedStep(step.getComments(), step.getKeyword(), step.getName().replaceAll(Tags.FULL, "").trim(), step.getLine(), step.getRows(), step.getDocString(), Type.Start);
    }

    public static ParameterizedStep asSubStep(Step step, String[] names, Object[] parameters) {
        return new ParameterizedStep(step.getComments(), step.getKeyword(), replacePlaceHolders(step.getName(), names, parameters), step.getLine(),
            step.getRows(), step.getDocString(), Type.SubStep);
    }

    public static ParameterizedStep parameterize(Step step, String[] names, Object[] parameters) {
        return new ParameterizedStep(step.getComments(), step.getKeyword(), replacePlaceHolders(step.getName(), names, parameters), step.getLine(),
            step.getRows(), step.getDocString(), Type.Parameterized);
    }

    public static ParameterizedStep endOf(Step step) {
        return new ParameterizedStep(step.getComments(), step.getKeyword(), step.getName().replaceAll(Tags.FULL, "").trim(), step.getLine(), step.getRows(), step.getDocString(), Type.End);
    }

}
