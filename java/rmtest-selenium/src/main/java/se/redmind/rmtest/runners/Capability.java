package se.redmind.rmtest.runners;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Jeremy Comte
 */
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Capability {

    String name();

    String value();
}
