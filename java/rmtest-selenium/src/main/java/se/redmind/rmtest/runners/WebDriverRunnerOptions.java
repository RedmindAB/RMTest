package se.redmind.rmtest.runners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jeremy Comte
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface WebDriverRunnerOptions {

    boolean reuseDriver() default true;

    Parallelize parallelize() default @Parallelize(drivers = false);

}
