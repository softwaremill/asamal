package pl.softwaremill.asamal.controller;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.seam.solder.core.Veto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Optionally injectable
 * <p/>
 * User: szimano
 */
@Veto
public class AsamalContext {
    public static final String FLASH_PREFIX = "flash.";

    private HttpServletRequest request;
    private HttpServletResponse response;
    private String[] extraPath = new String[0];
    private boolean willRedirect = false;
    private boolean willInclude = false;
    
    private String includeView;

    public AsamalContext() {
    }

    public AsamalContext(HttpServletRequest request, HttpServletResponse response,
                         String extraPath) {
        this.response = response;
        this.request = request;

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
    public void redirect(String controller, String view, PageParameters pageParameters) {
        if (willInclude || willRedirect) {
            throw new IllegalStateException("Include or redirect was already scheduled");
        }

        // do the redirect
        try {
            willRedirect = true;

            response.sendRedirect(request.getContextPath() + "/" + controller + "/" + view +
                    (pageParameters != null ? pageParameters.serialize() : ""));
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
        addMessageToFlash("*", msg, severity);
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

    public void addMessageToFlash(String key, String msg, MessageSeverity severity) {
        Multimap<String, String> msgsFlash = (Multimap<String, String>) request.getAttribute(FLASH_PREFIX +
                severity.name());
        Multimap<String, String> msgsNonFlash = (Multimap<String, String>) request.getAttribute(severity.name());

        // remember those when doing redirect
        if (msgsFlash == null) {
            msgsFlash = HashMultimap.create();
            request.setAttribute(FLASH_PREFIX + severity.name(), msgsFlash);
        }
        // remember those for includes
        if (msgsNonFlash == null) {
            msgsNonFlash = HashMultimap.create();
            request.setAttribute(severity.name(), msgsNonFlash);
        }

        msgsFlash.put(key, msg);
        msgsNonFlash.put(key, msg);
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
