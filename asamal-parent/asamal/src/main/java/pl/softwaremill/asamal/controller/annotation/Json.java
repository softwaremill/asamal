package pl.softwaremill.asamal.controller.annotation;

import javax.ws.rs.core.MediaType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks Json enabled methods
 *
 * User: szimano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ContentType(MediaType.APPLICATION_JSON)
public @interface Json {
}
