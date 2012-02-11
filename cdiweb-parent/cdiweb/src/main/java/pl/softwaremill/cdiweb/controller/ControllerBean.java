package pl.softwaremill.cdiweb.controller;

import org.apache.commons.beanutils.BeanUtils;
import pl.softwaremill.cdiweb.controller.annotation.Controller;

import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base Controller bean
 *
 * User: szimano
 */
public abstract class ControllerBean {
    
    private Map<String, Object> params = new HashMap<String, Object>();
    
    private String pageTitle = "CDIWEB Application";
    
    private String name;

    private CDIWebContext context;

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

    public void doPostMagic(java.util.Set<java.util.Map.Entry<String,List<String>>> entrySet) {
        for (Map.Entry<String, List<String>> entry : entrySet) {
            try {
                BeanUtils.setProperty(this, entry.getKey(), entry.getValue());
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

    public void redirect(String controller, String view) {
        context.redirect(controller, view);
    }

    public void redirect(String view) {
        context.redirect(getName(), view);
    }
    
    public String getParameter(String key) {
        return context.getParameter(key);
    }

    public List<String> getParameterValues(String key) {
        return context.getParameterValues(key);
    }

    public String[] getExtraPath() {
        return context.getExtraPath();
    }

    public Set<String> getParameterNames() {
        return context.getParameterNames();
    }
}
