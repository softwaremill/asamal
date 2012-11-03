package pl.softwaremill.asamal.controller.cdi;

import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.softwaremill.asamal.controller.AsamalFilter;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.FilterStopException;
import pl.softwaremill.asamal.controller.annotation.*;
import pl.softwaremill.asamal.controller.exception.AmbiguousViewMethodsException;
import pl.softwaremill.asamal.controller.exception.RequiredParameterNotFoundException;
import pl.softwaremill.asamal.extension.view.ResourceResolver;
import pl.softwaremill.common.util.dependency.D;

import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Class that resolves the controller beans and view actions
 * <p/>
 * User: szimano
 */
public class ControllerResolver {

    public static final String HTML_CONTENT_TYPE = MediaType.TEXT_HTML + "; charset=UTF-8";
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
            return possiblyProxiedMethod.invoke(controller, prepareMethodParameters(method, requestType));
        } else {
            return possiblyProxiedMethod.invoke(controller);
        }
    }

    private Object[] prepareMethodParameters(Method method, RequestType requestType)
            throws RequiredParameterNotFoundException {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] params = new Object[parameterTypes.length];
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        String pathParamDefinition = getPathParamDefinition(method, requestType);

        if (pathParamDefinition.startsWith("/")) {
            pathParamDefinition = pathParamDefinition.substring(1);
        }

        String[] pathParamKeys = pathParamDefinition.split("/");

        HashMap<String, String> pathParameters = new HashMap<String, String>();

        if (!pathParamDefinition.equals("")) {
            if (pathParamKeys.length > controller.getExtraPath().length) {
                throw new RequiredParameterNotFoundException(
                        "Path param definition is longer then the actual keys received. Expecting " +
                                Arrays.toString(pathParamKeys) + " but got " +
                                Arrays.toString(controller.getExtraPath()));
            }

            for (int i = 0; i < pathParamKeys.length; i++) {
                pathParameters.put(pathParamKeys[i], controller.getExtraPath()[i]);
            }
        }

        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] anns = parameterAnnotations[i];

            for (Annotation annotation : anns) {
                if (annotation.annotationType() == RequestParameter.class) {
                    // we should set this param

                    String key = ((RequestParameter) annotation).value();

                    if (String.class.isAssignableFrom(parameterTypes[i])) {
                        params[i] = controller.getParameter(key);
                    } else {
                        params[i] = controller.getObjectParameterValues(key);
                    }

                    // check if parameter wasn't required but is null
                    if (((RequestParameter) annotation).required() && params[i] == null) {
                        throw new RequiredParameterNotFoundException("Parameter " + key + " is required, but was not set");
                    }

                    continue;
                } else if (annotation.annotationType() == PathParameter.class) {
                    // this is param from extra path

                    String key = ((PathParameter) annotation).value();

                    if (!pathParameters.containsKey(key)) {
                        throw new RequiredParameterNotFoundException("Path parameter with name '" + key + "' was not " +
                                "defined in the Request Type annotation (" + requestType.getRequestAnnotation() + ")." +
                                " The param definition was: " + pathParamDefinition);
                    }

                    if (String.class.isAssignableFrom(parameterTypes[i])) {
                        params[i] = pathParameters.get(key);
                    } else {
                        params[i] = ConvertUtils.convert(pathParameters.get(key), parameterTypes[i]);
                    }

                    continue;
                }
            }
        }

        return params;
    }

    private String getPathParamDefinition(Method method, RequestType requestType) {
        try {
            Annotation requestAnnotation = method.getAnnotation(requestType.getRequestAnnotation());
            Method paramsMethod = requestType.getRequestAnnotation().getDeclaredMethod("params");

            return (String)paramsMethod.invoke(requestAnnotation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        if(System.getProperty(ResourceResolver.ASAMAL_DEV_DIR) != null) {
            // in DEV mode, ignore view hashes
            return true;
        }

        try {
            return findViewMethod(view, controller.getRealClass()).getAnnotation(Post.class).skipViewHash();
        } catch (AmbiguousViewMethodsException e) {
            throw new RuntimeException(e);
        }
    }

    public String contentType(String view) {
        try {
            Method viewMethod = findViewMethod(view, controller.getRealClass());

            for (Annotation annotation : viewMethod.getDeclaredAnnotations()) {
                ContentType contentType = annotation.annotationType().getAnnotation(ContentType.class);
                if (contentType != null) {
                    return contentType.value();
                }
            }

            return HTML_CONTENT_TYPE;
        } catch (AmbiguousViewMethodsException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isVoid(String view) {
        try {
            return findViewMethod(view, controller.getRealClass()).getReturnType().equals(Void.TYPE);
        } catch (AmbiguousViewMethodsException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
