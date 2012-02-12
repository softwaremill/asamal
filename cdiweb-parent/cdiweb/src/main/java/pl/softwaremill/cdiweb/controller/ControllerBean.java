package pl.softwaremill.cdiweb.controller;

import org.apache.commons.beanutils.BeanUtils;
import pl.softwaremill.cdiweb.controller.annotation.Controller;

import javax.inject.Inject;
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

    private String pageTitle = "CDIWEB Application";

    private String name;

    @Inject
    protected CDIWebContext context;

    protected ControllerBean() {
    }

    public void setContext(CDIWebContext context) {
        this.context = context;
    }

    public String getName() {
        if (name == null) {
            // read it
            name = this.getClass().getAnnotation(Controller.class).value();
        }

        return name;
    }

    protected void setParameter(String key, Object value) {
        params.put(key, value);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void clearParams() {
        params.clear();
    }

    protected void doAutoBinding(String... parameterNames) {
        for (String parameterName : parameterNames) {
            try {
                BeanUtils.setProperty(this, parameterName, getParameterValues(parameterName));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
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
     * @param view Name of the view
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
     *
     * If the link was /home/index/this/is/it this will return a list of String[]{"this", "is", "it"}
     *
     * @return List of elements
     */
    public String[] getExtraPath() {
        return context.getExtraPath();
    }

    /**
     * Adds a message to the flash scope, so they will be visibile after redirect.
     *
     * The messages will be added also inside the current request, to be visible if include is called.
     *
     * The messages will be then available in the velocity scope under $info, $err, $success and $warn (lists).
     *
     * @param msg Message
     * @param severity Severity
     */
    public void addMessageToFlash(String msg, CDIWebContext.MessageSeverity severity) {
        context.addMessageToFlash(msg, severity);
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
     *
     * It will be accessible in this and the next reques.
     *
     * @param key Key
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

    public CDIWebContext getContext() {
        return context;
    }

    public void redirectToURI(String uri) {
        context.redirectToURI(uri);
    }
}
