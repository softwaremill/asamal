package pl.softwaremill.asamal.controller.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.softwaremill.asamal.controller.AsamalFilter;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.FilterStopException;
import pl.softwaremill.asamal.controller.annotation.ControllerImpl;
import pl.softwaremill.asamal.controller.annotation.Filters;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Json;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.controller.annotation.RequestParameter;
import pl.softwaremill.asamal.controller.exception.AmbiguousViewMethodsException;
import pl.softwaremill.asamal.controller.exception.RequiredParameterNotFoundException;
import pl.softwaremill.common.util.dependency.D;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Class that resolves the controller beans and view actions
 * <p/>
 * User: szimano
 */
public class ControllerResolver {

    private ControllerBean controller;

    private final static Logger log = LoggerFactory.getLogger(ControllerResolver.class);

    public static ControllerResolver resolveController(String controllerName) throws FilterStopException {
        ControllerResolver resolver = new ControllerResolver();

        resolver.controller = D.inject(ControllerBean.class, new ControllerImpl(controllerName));

        // check controller-wide filters first
        Filters filters = resolver.controller.getRealClass().getAnnotation(Filters.class);

        if (filters != null) {
            for (Class<? extends AsamalFilter> filterClass : filters.value()) {
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
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, FilterStopException,
            AmbiguousViewMethodsException, RequiredParameterNotFoundException {
        // get the view method
        Method method = findViewMethod(view, controller.getRealClass());
        Method possiblyProxiedMethod = controller.getClass().getDeclaredMethod(view, method.getParameterTypes());

        // check method filters
        Filters filters = method.getAnnotation(Filters.class);
        if (filters != null) {
            for (Class<? extends AsamalFilter> filterClass : filters.value()) {
                D.inject(filterClass).doFilter();

                // if redirect scheduled - stop the execution righ away
                if (controller.getContext().isWillRedirect()) {
                    throw new FilterStopException();
                }
            }
        }

        if (method.getAnnotation(requestType.getRequestAnnotation()) == null) {
            throw new SecurityException("Method " + view + " on controller " + controller.getClass().getSimpleName() +
                    " isn't annotated with @" +
                    requestType.getRequestAnnotation().getSimpleName());
        }

        // invoke it
        if (possiblyProxiedMethod.getParameterTypes().length > 0) {
            return possiblyProxiedMethod.invoke(controller, prepareMethodParameters(possiblyProxiedMethod));
        }
        else {
            return possiblyProxiedMethod.invoke(controller);
        }
    }

    private Object[] prepareMethodParameters(Method method) throws RequiredParameterNotFoundException {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] params = new Object[parameterTypes.length];
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] anns = parameterAnnotations[i];

            for (Annotation annotation : anns) {
                if (annotation.annotationType() == RequestParameter.class) {
                    // we should set this param

                    String key = ((RequestParameter) annotation).name();

                    if (String.class.isAssignableFrom(parameterTypes[i])) {
                        params[i] = controller.getParameter(key);
                    }
                    else {
                        params[i] = controller.getObjectParameterValues(key);
                    }

                    // check if parameter wasn't required but is null
                    if (((RequestParameter) annotation).required() && params[i] == null) {
                        throw new RequiredParameterNotFoundException("Parameter "+key+" is required, but was not set");
                    }

                    continue;
                }
            }
        }

        return params;
    }

    private Method findViewMethod(String view, Class<? extends ControllerBean> beanClass)
            throws AmbiguousViewMethodsException, NoSuchMethodException {
        Set<Method> foundMethods = new HashSet<Method>();

        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.getName().equals(view)) {
                foundMethods.add(method);
            }
        }

        if (foundMethods.size() > 1) {
            // we need to quess cleverly - maybe one has asamal's annotation, and the others do not
            Iterator<Method> it = foundMethods.iterator();

            while (it.hasNext()) {
                Method method = it.next();

                if (method.getAnnotation(Get.class) == null && method.getAnnotation(Post.class) == null
                        && method.getAnnotation(Json.class) == null) {
                    it.remove();
                }
            }
        }

        // now if we still have more then one, we have to fail
        if (foundMethods.size() > 1) {
            throw new AmbiguousViewMethodsException("Ambiguous methods found: " + foundMethods.toString());
        }

        if (foundMethods.isEmpty()) {
            throw new NoSuchMethodException("Cannot find method to match view " + view +
                    " annotated with any asamal annotation");
        }

        return foundMethods.iterator().next();
    }

    public ControllerBean getController() {
        return controller;
    }

    public boolean skipViewHash(String view) throws NoSuchMethodException {
        return controller.getRealClass().getDeclaredMethod(view).getAnnotation(Post.class).skipViewHash();
    }
}
