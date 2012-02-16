package pl.softwaremill.asamal.controller.annotation;

import java.lang.annotation.Annotation;

/**
 * User: szimano
 */
public class ControllerImpl implements Controller{
    private String value;

    public ControllerImpl(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public Class<? extends Annotation> annotationType() {
        return Controller.class;
    }
}
