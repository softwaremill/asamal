package pl.softwaremill.asamal.controller;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.exception.AutobindingException;
import pl.softwaremill.asamal.controller.exception.NoSuchParameterException;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base Controller bean
 * <p/>
 * User: szimano
 */
public abstract class ControllerBean {

    private Map<String, Object> params = new HashMap<String, Object>();

    private String pageTitle = "Asamal Application";

    private String name;

    @Inject
    protected AsamalContext context;

    protected ControllerBean() {
    }

    public void setContext(AsamalContext context) {
        this.context = context;
    }

    public String getName() {
        if (name == null) {
            name = this.getRealClass().getAnnotation(Controller.class).value();
        }

        return name;
    }

    protected void putInContext(String key, Object value) {
        params.put(key, value);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void clearParams() {
        params.clear();
    }

    protected void doAutoBinding(String... parameterNames) {
        doAutoBinding(false, parameterNames);
    }

    protected void doOptionalAutoBinding(String... parameterNames) {
        doAutoBinding(true, parameterNames);
    }

    protected void doAutoBinding(boolean optionalParams, String... parameterNames) {
        for (String parameterName : parameterNames) {
            if (!getParameterNames().contains(parameterName)) {
                if (!optionalParams) {
                    throw new NoSuchParameterException("There is no parameter " + parameterName);
                }
            } else {
                try {
                    List<Object> values = getObjectParameterValues(parameterName);

                    Object toSet = values;

                    if (!Collection.class.isAssignableFrom(
                            PropertyUtils.getPropertyDescriptor(this, parameterName).getPropertyType()) &&
                            values.size() == 1) {
                        // if field is not a collection and only one element, unpack it
                        toSet = values.get(0);
                    }

                    BeanUtils.setProperty(this, parameterName, toSet);
                } catch (Exception e) {
                    throw new AutobindingException(e);
                }
            }
        }
    }

    private List<Object> getObjectParameterValues(String parameterName) {
        return context.getObjectParameterValues(parameterName);
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    /**
     * Will redirect the view to a new controller/view
     *
     * @param controller Name of the controller
     * @param view       Name of the view
     * @throws IllegalStateException If includeView was already scheduled
     */
    public void redirect(String controller, String view) {
        context.redirect(controller, view);
    }

    /**
     * Will redirect the view to a new one (will use the same controller)
     *
     * @param view Name of the view
     * @throws IllegalStateException If includeView was already scheduled
     */
    public void redirect(String view) {
        redirect(getName(), view);
    }

    /**
     * Gets the parameter's single (or first) value
     *
     * @param key Name of the parameter
     * @return Value
     */
    public String getParameter(String key) {
        return context.getParameter(key);
    }

    /**
     * Gets all the values of a single parameter
     *
     * @param key Name of the parameter
     * @return List of values
     */
    public List<String> getParameterValues(String key) {
        return context.getParameterValues(key);
    }

    /**
     * Gets all the parameter's names available from the post/get
     *
     * @return Set of parameter names
     */
    public Set<String> getParameterNames() {
        return context.getParameterNames();
    }

    /**
     * Gets all the extra elements (if any) after /view/controller path.
     * <p/>
     * If the link was /home/index/this/is/it this will return a list of String[]{"this", "is", "it"}
     *
     * @return List of elements
     */
    public String[] getExtraPath() {
        return context.getExtraPath();
    }

    /**
     * Adds a message to the flash scope, so they will be visibile after redirect.
     * <p/>
     * The messages will be added also inside the current request, to be visible if include is called.
     * <p/>
     * The messages will be then available in the velocity scope under $info, $err, $success and $warn (lists).
     *
     * @param msg      Message
     * @param severity Severity
     */
    public void addMessageToFlash(String msg, AsamalContext.MessageSeverity severity) {
        context.addMessageToFlash(msg, severity);
    }

    /**
     * Will add a message under a specific key into the flash scope
     */
    public void addMessageToFlash(String key, String msg, AsamalContext.MessageSeverity severity) {
        context.addMessageToFlash(key, msg, severity);
    }

    /**
     * This will include another view once the view's method is finished
     *
     * @param view View to include
     * @throws IllegalStateException If redirect was already scheduled
     */
    public void includeView(String view) {
        context.includeView(view);
    }

    /**
     * Adds object to the flash scope.
     * <p/>
     * It will be accessible in this and the next reques.
     *
     * @param key   Key
     * @param value The object
     */
    public void addObjectToFlash(String key, Object value) {
        context.addObjectToFlash(key, value);
    }

    /**
     * Retrieves object from the flash scope
     *
     * @param key Key
     * @return the previously put object or null
     */
    public Object getObjectFromFlash(String key) {
        return context.getObjectFromFlash(key);
    }

    public AsamalContext getContext() {
        return context;
    }

    public void redirectToURI(String uri) {
        context.redirectToURI(uri);
    }

    public Object getObjectParameter(String key) {
        return context.getObjectParameter(key);
    }

    /**
     * This method will return a class of this object handling possible Proxy.
     */
    public Class<? extends ControllerBean> getRealClass() {
        if (getClass().getAnnotation(Controller.class) != null) {
            return getClass();
        } else {
            return (Class<? extends ControllerBean>) getClass().getSuperclass();
        }
    }
    
    public <T> boolean validateBean(String validationPrefix, T bean) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<T>> violations = validator.validate(bean);

        for (ConstraintViolation<T> violation : violations) {
            addMessageToFlash(validationPrefix + "." + violation.getPropertyPath().toString(),
                    violation.getMessage(),
                    AsamalContext.MessageSeverity.ERR);
        }
        return violations.isEmpty();
    }
}
