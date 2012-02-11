package pl.softwaremill.cdiweb.controller.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.softwaremill.cdiweb.controller.CDIWebContext;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.ControllerImpl;
import pl.softwaremill.common.util.dependency.D;

import javax.enterprise.context.SessionScoped;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * User: szimano
 */
public class ControllerResolver {

    private ControllerBean controller;

    private final static Logger log = LoggerFactory.getLogger(ControllerResolver.class);

    public static ControllerResolver resolveController(String controllerName) {
        ControllerResolver resolver = new ControllerResolver();

        resolver.controller = D.inject(ControllerBean.class, new ControllerImpl(controllerName));

        return resolver;
    }

    public Object executeView(RequestType requestType, String view, CDIWebContext context)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // get the view method
        Method method = controller.getClass().getDeclaredMethod(view);

        // check that the method is annotated with the correct annotation
        if (method.getAnnotation(requestType.getRequestAnnotation()) == null) {
            throw new SecurityException("Method " + view + " on controller " + controller.getClass().getSimpleName() +
                    " isn't annotated with @" +
                    requestType.getRequestAnnotation().getSimpleName());
        }

        // set the context
        controller.setContext(context);

        // invoke it
        return method.invoke(controller);
    }

    public ControllerBean getController() {
        return controller;
    }
}
