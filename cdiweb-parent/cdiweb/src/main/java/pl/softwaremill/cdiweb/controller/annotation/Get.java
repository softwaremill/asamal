package pl.softwaremill.cdiweb.controller.annotation;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation specifies controller
 *
 * User: szimano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Qualifier
public @interface Get {
}
