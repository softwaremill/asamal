package pl.softwaremill.asamal.controller.cdi;

import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.extension.AsamalExtension;
import pl.softwaremill.asamal.extension.view.PresentationExtension;

import javax.enterprise.context.NormalScope;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Named;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Bootstrap CDI extension to check that the configuration is right
 *
 * User: szimano
 */
public class AsamalAnnotationScanner implements Extension, Serializable {

    private Set<Class> namedBeans = new HashSet<Class>();

    private Set<Class<? extends PresentationExtension>> presentationExtensions =
            new HashSet<Class<? extends PresentationExtension>>();

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

            // remember web scoped beans
            if (Named.class.isAssignableFrom(annotation.annotationType())) {
                namedBeans.add(event.getAnnotatedType().getJavaClass());
            }

            // remember PresentationExtensions
            if (AsamalExtension.class.isAssignableFrom(annotation.annotationType())) {
                presentationExtensions.add(
                        (Class<? extends PresentationExtension>) event.getAnnotatedType().getJavaClass());
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

    public Set<Class> getNamedBeans() {
        return namedBeans;
    }

    public Set<Class<? extends PresentationExtension>> getPresentationExtensions() {
        return presentationExtensions;
    }
}
