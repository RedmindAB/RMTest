package se.redmind.rmtest.runners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;

import se.redmind.rmtest.WebDriverWrapper;
import se.redmind.rmtest.selenium.framework.Browser;

/**
 * If a test class or a method is annotated with this annotation, it will be filtered and run only if the current configuration matches the filter.
 *
 * @author Jeremy Comte
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface FilterDrivers {

    Platform[] platforms() default {};

    Capability[] capabilities() default {};

    Browser[] browsers() default {};

    Class<? extends WebDriver>[] types() default {};

    Class<? extends Predicate<WebDriverWrapper<?>>> filter() default AlwaysTrue.class;

    class AlwaysTrue implements Predicate<WebDriverWrapper<?>> {

        @Override
        public boolean test(WebDriverWrapper<?> t) {
            return true;
        }

    }

}
