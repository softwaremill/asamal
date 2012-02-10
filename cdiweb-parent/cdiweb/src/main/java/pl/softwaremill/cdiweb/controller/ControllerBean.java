package pl.softwaremill.cdiweb.controller;

import org.apache.commons.beanutils.BeanUtils;

import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base Controller bean
 *
 * User: szimano
 */
public abstract class ControllerBean {
    
    private Map<String, Object> params = new HashMap<String, Object>();
    
    private String pageTitle = "CDIWEB Application";

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
}
