package se.redmind.rmtest.selenium.grid.rules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Assume;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class ConditionalRule implements MethodRule {

    public interface IgnoreCondition {

        boolean isSatisfied();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface ConditionalIgnore {

        Class<? extends IgnoreCondition> condition();
    }

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        Statement result = base;
        if (hasConditionalIgnoreAnnotation(method)) {
            IgnoreCondition condition = getIgnoreContition(method, target);
            if (condition.isSatisfied()) {
                result = new IgnoreStatement(condition);
            }
        }
        return result;
    }

    private boolean hasConditionalIgnoreAnnotation(FrameworkMethod method) {
        return method.getAnnotation(ConditionalIgnore.class) != null;
    }

    private IgnoreCondition getIgnoreContition(FrameworkMethod method, Object instance) {
        ConditionalIgnore annotation = method.getAnnotation(ConditionalIgnore.class);
        return newCondition(annotation, instance);
    }

    private IgnoreCondition newCondition(ConditionalIgnore annotation, Object instance) {
        final Class<? extends IgnoreCondition> cond = annotation.condition();
        try {
            if (cond.isMemberClass()) {
                if (Modifier.isStatic(cond.getModifiers())) {
                    return (IgnoreCondition) cond.getDeclaredConstructor(new Class<?>[]{}).newInstance();
                } else if (instance != null && instance.getClass().isAssignableFrom(cond.getDeclaringClass())) {
                    return (IgnoreCondition) cond.getDeclaredConstructor(new Class<?>[]{instance.getClass()}).newInstance(instance);
                }
                throw new IllegalArgumentException("Conditional class: " + cond.getName() + " was an inner member class however it was not declared inside the"
                    + " test case using it. Either make this class a static class (by adding static keyword),"
                    + " standalone class (by declaring it in it's own file) or move it inside the test case using it");
            } else {
                return cond.newInstance();
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException |
            IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static class IgnoreStatement extends Statement {

        private final IgnoreCondition condition;

        IgnoreStatement(IgnoreCondition condition) {
            this.condition = condition;
        }

        @Override
        public void evaluate() {
            Assume.assumeTrue("Ignored by " + condition.getClass().getSimpleName(), false);
        }
    }
}
