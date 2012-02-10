package pl.softwaremill.cdiweb.cdi;

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

    public ControllerResolver executeView(String view) throws InvocationTargetException, IllegalAccessException,
            NoSuchMethodException {
        // get the view method
        Method method = controller.getClass().getDeclaredMethod(view);

        // invoke it
        method.invoke(controller);

        return this;
    }

    public ControllerBean getController() {
        return controller;
    }
}