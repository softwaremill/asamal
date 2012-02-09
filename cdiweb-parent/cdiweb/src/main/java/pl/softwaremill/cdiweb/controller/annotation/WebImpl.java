package pl.softwaremill.cdiweb.controller.annotation;

import javax.enterprise.util.Nonbinding;
import javax.inject.Named;
import java.lang.annotation.Annotation;

/**
 * User: szimano
 */
public class WebImpl implements Web {

    public String value() {
        return null;
    }

    public Class<? extends Annotation> annotationType() {
        return Web.class;
    }
}
