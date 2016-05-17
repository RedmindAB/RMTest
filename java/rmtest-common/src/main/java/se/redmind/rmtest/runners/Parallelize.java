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
public @interface Parallelize {

    /**
     * -1 for (Runtime.getRuntime().availableProcessors() / 2) + 1
     *
     * @return the amount of threads that will be used to parallelize the tests
     */
    int threads() default -1;

    boolean drivers() default true;

    boolean tests() default false;

}
