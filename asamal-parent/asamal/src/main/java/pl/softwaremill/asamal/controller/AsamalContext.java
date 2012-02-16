package pl.softwaremill.asamal.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Optionally injectable
 * <p/>
 * User: szimano
 */
public class AsamalContext {
    private static final String FLASH_PREFIX = "flash.";

    private HttpServletRequest request;
    private HttpServletResponse response;
    private String[] extraPath = new String[0];
    private MultivaluedMap<String, Object> formValueMap;
    private boolean willRedirect = false;
    private boolean willInclude = false;
    
    private String includeView;

    public AsamalContext(HttpServletRequest request, HttpServletResponse response,
                         String extraPath, MultivaluedMap<String, Object> formValueMap) {
        this.response = response;
        this.request = request;
        this.formValueMap = formValueMap;

        if (extraPath != null) {
            this.extraPath = extraPath.split("/");
        }
    }

    /**
     * Will redirect the view to a new controller/view
     *
     * @param controller Name of the controller
     * @param view Name of the view
     * @throws IllegalStateException If includeView was already scheduled
     */
    public void redirect(String controller, String view) {
        if (willInclude || willRedirect) {
            throw new IllegalStateException("Include or redirect was already scheduled");
        }

        // do the redirect
        try {
            willRedirect = true;

            response.sendRedirect(request.getContextPath() + "/" + controller + "/" + view);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void redirectToURI(String uri) {
        if (willInclude || willRedirect) {
            throw new IllegalStateException("Include or redirect was already scheduled");
        }

        // do the redirect
        try {
            willRedirect = true;

            response.sendRedirect(uri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets the parameter's single (or first) value
     *
     * @param key Name of the parameter
     * @return Value
     */
    public String getParameter(String key) {
        if (formValueMap == null) {
            String[] values = request.getParameterMap().get(key);

            return (values.length == 0 ? null : values[0]);
        }
        return (String) formValueMap.getFirst(key);
    }
    public Object getObjectParameter(String key) {
        if (formValueMap == null) {
            String[] values = request.getParameterMap().get(key);

            return (values.length == 0 ? null : values[0]);
        }
        return formValueMap.getFirst(key);
    }

    /**
     * Gets all the values of a single parameter
     *
     * @param key Name of the parameter
     * @return List of values
     */
    public List<String> getParameterValues(String key) {
        if (formValueMap == null) {
            return Arrays.asList(request.getParameterMap().get(key));
        }
        List<String> values = new ArrayList<String>();
        for (Object obj : formValueMap.get(key)) {
            values.add(obj.toString());
        }
        return values;
    }

    /**
     * Gets all the parameter's names available from the post/get
     *
     * @return Set of parameter names
     */
    public Set<String> getParameterNames() {
        if (formValueMap == null) {
            return request.getParameterMap().keySet();
        }
        return formValueMap.keySet();
    }

    /**
     * Gets all the extra elements (if any) after /view/controller path.
     *
     * If the link was /home/index/this/is/it this will return a list of String[]{"this", "is", "it"}
     *
     * @return List of elements
     */
    public String[] getExtraPath() {
        return extraPath;
    }

    public boolean isWillRedirect() {
        return willRedirect;
    }

    public boolean isWillInclude() {
        return willInclude;
    }

    public String getIncludeView() {
        return includeView;
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
    public void addMessageToFlash(String msg, MessageSeverity severity) {
        List<String> msgsFlash = (List<String>) request.getAttribute(FLASH_PREFIX + severity.name());
        List<String> msgsNonFlash = (List<String>) request.getAttribute(severity.name());

        // remember those when doing redirect
        if (msgsFlash == null) {
            msgsFlash = new ArrayList<String>();
            request.setAttribute(FLASH_PREFIX + severity.name(), msgsFlash);
        }
        // remember those for includes
        if (msgsNonFlash == null) {
            msgsNonFlash = new ArrayList<String>();
            request.setAttribute(severity.name(), msgsNonFlash);
        }

        msgsFlash.add(msg);
        msgsNonFlash.add(msg);
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
        request.setAttribute(FLASH_PREFIX + key, value);

        // put also in just request, to be accessible in includes
        request.setAttribute(key, value);
    }

    /**
     * Retrieves object from the flash scope
     *
     * @param key Key
     * @return the previously put object or null
     */
    public Object getObjectFromFlash(String key) {
        return request.getAttribute(key);
    }

    /**
     * This will include another view once the view's method is finished
     *
     * @param view View to include
     * @throws IllegalStateException If redirect was already scheduled
     */
    public void includeView(String view) {
        if (willRedirect || willInclude) {
            throw new IllegalStateException("Include or redirect was already scheduled");
        }
        includeView = view;
        willInclude = true;
    }

    /**
     * Gets the current link
     *
     * @return Current link
     */
    public String getCurrentLink() {
        String queryString = request.getQueryString();
        return request.getRequestURI() + (queryString == null ? "" : "?" + queryString);
    }

    public List<Object> getObjectParameterValues(String parameterName) {
        if (formValueMap == null) {
            List<Object> o = new ArrayList<Object>();
            for (String param : request.getParameterValues(parameterName)) {
                o.add(param);
            }
            return o;
        }
        return formValueMap.get(parameterName);
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
