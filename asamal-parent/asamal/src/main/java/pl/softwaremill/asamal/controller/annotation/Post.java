package pl.softwaremill.asamal.controller.annotation;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks POST enabled methods
 *
 * User: szimano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Qualifier
public @interface Post {
}
