package pl.softwaremill.cdiweb.controller.annotation;

import javax.inject.Qualifier;
import javax.ws.rs.Path;
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
@Target(ElementType.TYPE)
@Qualifier
public @interface Controller{

    /**
     * @return Name of the controller
     */
    String value();
}
