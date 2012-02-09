package pl.softwaremill.cdiweb.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Base Controller bean
 *
 * User: szimano
 */
public abstract class ControllerBean {
    
    private Map<String, Object> params = new HashMap<String, Object>();

    protected void setParameter(String key, Object value) {
        params.put(key, value);
    }
    
    public Map<String, Object> getParams() {
        return params;
    }

    public void clearParams() {
        params.clear();
    }
}
