package pl.softwaremill.asamal.controller.annotation;

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
