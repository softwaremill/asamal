package pl.softwaremill.cdiweb.controller.cdi;

import pl.softwaremill.cdiweb.controller.CDIWebContext;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.ControllerImpl;
import pl.softwaremill.common.util.dependency.D;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * User: szimano
 */
public class ControllerResolver {

    private ControllerBean controller;

    public static ControllerResolver resolveController(String controllerName) {
        ControllerResolver resolver = new ControllerResolver();

        resolver.controller = D.inject(ControllerBean.class, new ControllerImpl(controllerName));

        return resolver;
    }

    public Object executeView(RequestType requestType, String view, CDIWebContext context)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // get the view method
        Method method = null;
        try {
            method = controller.getClass().getDeclaredMethod(view);
        } catch (NoSuchMethodException e) {
            // try with context param
            method = controller.getClass().getDeclaredMethod(view, CDIWebContext.class);
        }

        // check that the method is annotated with the correct annotation
        if (method.getAnnotation(requestType.getRequestAnnotation()) == null) {
            throw new SecurityException("Method " + view + " on controller " + controller.getClass().getSimpleName() +
                    " isn't annotated with @" +
                    requestType.getRequestAnnotation().getSimpleName());
        }

        // invoke it
        return invokeMethod(method, controller, context);
    }

    public ControllerBean getController() {
        return controller;
    }

    private Object invokeMethod(Method method, ControllerBean controller, CDIWebContext context)
            throws InvocationTargetException, IllegalAccessException {
        // check if it has parameters
        if (method.getParameterTypes().length > 0) {
            return method.invoke(controller, context);
        }

        return method.invoke(controller);
    }
}
