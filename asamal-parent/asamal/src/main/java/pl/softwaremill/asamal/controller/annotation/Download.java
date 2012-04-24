package pl.softwaremill.asamal.controller.annotation;

import javax.inject.Qualifier;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks get enabled methods
 *
 * User: szimano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Qualifier
public @interface Download {

    String params() default "";

    String contentType() default MediaType.APPLICATION_OCTET_STREAM;
}
