package pl.softwaremill.cdiweb.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Optionally injectable
 *
 * User: szimano
 */
public class CDIWebContext {
    private ControllerBean controllerBean;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private String[] extraPath = new String[0];

    public CDIWebContext(HttpServletRequest request, HttpServletResponse response, ControllerBean controllerBean, 
                         String extraPath) {
        this.response = response;
        this.request = request;
        this.controllerBean = controllerBean;

        if (extraPath != null) {
            this.extraPath = extraPath.split("/");
        }
    }

    public void redirect(String controller, String view) {
        // do the redirect
        try {
            response.sendRedirect(request.getContextPath() + "/" + controller + "/" + view);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void redirect(String view) {
        // do the redirect on teh current controller
        redirect(controllerBean.getName(), view);
    }
    
    public String getParameter(String key) {
        return request.getParameter(key);
    }

    public String[] getParameterValues(String key) {
        return request.getParameterValues(key);
    }
    
    public String[] getExtraPath() {
       return extraPath;
    }
}
