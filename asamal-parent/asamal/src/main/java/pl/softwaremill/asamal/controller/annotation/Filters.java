package pl.softwaremill.asamal.controller.annotation;

import pl.softwaremill.asamal.controller.AsamalFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets the filters to be run either on a controller (will be run for all actions) or single action
 *
 * User: szimano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Filters {
    
    public Class<? extends AsamalFilter>[] value();
}
