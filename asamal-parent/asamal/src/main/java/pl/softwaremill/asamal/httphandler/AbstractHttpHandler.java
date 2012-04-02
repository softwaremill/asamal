package pl.softwaremill.asamal.httphandler;

import pl.softwaremill.asamal.AsamalParameters;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.producers.AsamalProducers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;

/**
 * User: szimano
 */
public class AbstractHttpHandler {

    protected AsamalProducers asamalProducers;

    public AbstractHttpHandler(AsamalProducers asamalProducers) {
        this.asamalProducers = asamalProducers;
    }

    public AbstractHttpHandler() {
    }

    protected AsamalContext createContext(HttpServletRequest req, HttpServletResponse resp, String extraPath) {
        AsamalContext context = new AsamalContext(req, resp, extraPath);
        asamalProducers.setAsamalContext(context);

        return context;
    }

    protected void createParamateres(HttpServletRequest req, MultivaluedMap<String, Object> formValues) {
        asamalProducers.setAsamalParameters(new AsamalParameters(req, formValues));
    }
    
    protected void setHttpObjects(HttpServletRequest request, HttpServletResponse response) {
        asamalProducers.setHttpObjects(request, response);
    }
}
