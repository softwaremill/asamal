package pl.softwaremill.cdiweb.controller;

import pl.softwaremill.cdiweb.servlet.FlashScopeFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.*;

/**
 * Optionally injectable
 * <p/>
 * User: szimano
 */
public class CDIWebContext {
    private static final String FLASH_PREFIX = "flash.";

    private HttpServletRequest request;
    private HttpServletResponse response;
    private String[] extraPath = new String[0];
    private MultivaluedMap<String, String> formValueMap;
    private boolean willRedirect = false;

    public CDIWebContext(HttpServletRequest request, HttpServletResponse response,
                         String extraPath, MultivaluedMap<String, String> formValueMap) {
        this.response = response;
        this.request = request;
        this.formValueMap = formValueMap;

        if (extraPath != null) {
            this.extraPath = extraPath.split("/");
        }
    }

    public void redirect(String controller, String view) {
        // do the redirect
        try {
            willRedirect = true;

            response.sendRedirect(request.getContextPath() + "/" + controller + "/" + view);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getParameter(String key) {
        if (formValueMap == null) {
            String[] values = request.getParameterMap().get(key);

            return (values.length == 0 ? null : values[0]);
        }
        return formValueMap.getFirst(key);
    }

    public List<String> getParameterValues(String key) {
        if (formValueMap == null) {
            return Arrays.asList(request.getParameterMap().get(key));
        }
        return formValueMap.get(key);
    }
    
    public Set<String> getParameterNames() {
        if (formValueMap == null) {
            return request.getParameterMap().keySet();
        }
        return formValueMap.keySet();
    }

    public String[] getExtraPath() {
        return extraPath;
    }

    public boolean isWillRedirect() {
        return willRedirect;
    }
    
    public void addMessageToFlash(String msg, MessageSeverity severity) {
        List<String> msgs = (List<String>) request.getAttribute(FLASH_PREFIX + severity.name());
       
        if (msgs == null) {
            msgs = new ArrayList<String>();

            request.setAttribute(FLASH_PREFIX + severity.name(), msgs);
        }
        msgs.add(msg);
    }

    public enum MessageSeverity {
        /**
         * Information
         */
        INFO,

        /**
         * Warning
         */
        WARN,

        /**
         * Error
         */
        ERR,

        /**
         * Success
         */
        SUCCESS;
    }
}
