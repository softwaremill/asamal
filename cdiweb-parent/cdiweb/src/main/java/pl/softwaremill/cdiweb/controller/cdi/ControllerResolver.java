package pl.softwaremill.cdiweb.controller.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.softwaremill.cdiweb.controller.CDIWebFilter;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.FilterStopException;
import pl.softwaremill.cdiweb.controller.annotation.ControllerImpl;
import pl.softwaremill.cdiweb.controller.annotation.Filters;
import pl.softwaremill.common.util.dependency.D;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * User: szimano
 */
public class ControllerResolver {

    private ControllerBean controller;

    private final static Logger log = LoggerFactory.getLogger(ControllerResolver.class);

    public static ControllerResolver resolveController(String controllerName) throws FilterStopException {
        ControllerResolver resolver = new ControllerResolver();

        resolver.controller = D.inject(ControllerBean.class, new ControllerImpl(controllerName));
        
        // check controller-wide filters first
        Filters filters = resolver.controller.getClass().getAnnotation(Filters.class);

        if (filters != null) {
            for (Class<? extends CDIWebFilter> filterClass : filters.value()) {
                D.inject(filterClass).doFilter();

                // if redirect scheduled - stop the execution righ away
                if (resolver.controller.getContext().isWillRedirect()) {
                    throw new FilterStopException();
                }
            }
        }

        return resolver;
    }

    public Object executeView(RequestType requestType, String view)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, FilterStopException {
        // get the view method
        Method method = controller.getClass().getDeclaredMethod(view);
        
        // check method filters
        Filters filters = method.getAnnotation(Filters.class);
        if (filters != null) {
            for (Class<? extends CDIWebFilter> filterClass : filters.value()) {
                D.inject(filterClass).doFilter();

                // if redirect scheduled - stop the execution righ away
                if (controller.getContext().isWillRedirect()) {
                    throw new FilterStopException();
                }
            }
        }

        // check that the method is annotated with the correct annotation
        if (method.getAnnotation(requestType.getRequestAnnotation()) == null) {
            throw new SecurityException("Method " + view + " on controller " + controller.getClass().getSimpleName() +
                    " isn't annotated with @" +
                    requestType.getRequestAnnotation().getSimpleName());
        }

        // invoke it
        return method.invoke(controller);
    }

    public ControllerBean getController() {
        return controller;
    }
}
