package pl.softwaremill.asamal.controller.annotation;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks DELETE enabled methods
 *
 * User: szimano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Qualifier
public @interface Delete {

    /**
     * If this is set to true, then application won't require the viewHash to be present.
     *
     * This will be ignored for reRender posts.
     */
    boolean skipViewHash() default false;

    String params() default "";
}
