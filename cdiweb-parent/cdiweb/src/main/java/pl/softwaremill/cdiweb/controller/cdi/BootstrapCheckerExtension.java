package pl.softwaremill.cdiweb.controller.cdi;

import com.sun.tools.javac.comp.Check;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;

import javax.enterprise.context.NormalScope;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * Bootstrap CDI extension to check that the configuration is right
 *
 * User: szimano
 */
public class BootstrapCheckerExtension implements Extension, Serializable {

    public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> event) {
        for (Annotation annotation : event.getAnnotatedType().getAnnotations()) {
            if (Controller.class.isAssignableFrom(annotation.annotationType())) {
                // check that the bean is ControllerBean

                if (!ControllerBean.class.isAssignableFrom(event.getAnnotatedType().getJavaClass())) {
                    throw new RuntimeException("The @Controller class has to extend the ControllerBean. Check "
                            + event.getAnnotatedType().getJavaClass());
                }

                if (isNonDefaultScope(event.getAnnotatedType().getJavaClass())) {
                    throw new RuntimeException("The @Controller class has to be specified in @Default scope. Check "
                            + event.getAnnotatedType().getJavaClass());
                }
            }
        }
    }

    private <T> boolean isNonDefaultScope(Class<T> javaClass) {
        for (Annotation annotation : javaClass.getAnnotations()) {
            if (annotation.annotationType().getAnnotation(NormalScope.class) != null) {
                return true;
            }
        }

        return false;
    }
}
