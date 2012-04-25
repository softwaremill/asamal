package pl.softwaremill.asamal.producers;

import pl.softwaremill.asamal.AsamalParameters;
import pl.softwaremill.asamal.controller.AsamalContext;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestScoped
public class AsamalProducersImpl implements AsamalProducers {

    private AsamalContext asamalContext;
    private AsamalParameters asamalParameters;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @Produces
    public AsamalContext produceAsamalContext() {
        return asamalContext;
    }

    @Produces
    public AsamalParameters produceAsamalParamateres() {
        return asamalParameters;
    }

    @Produces
    public HttpServletRequest produceServletRequest() {
        return request;
    }

    @Produces
    public HttpServletResponse produceServletResponse() {
        return response;
    }

    public void setAsamalContext(AsamalContext asamalContext) {
        this.asamalContext = asamalContext;
    }

    public void setAsamalParameters(AsamalParameters asamalParameters) {
        this.asamalParameters = asamalParameters;
    }

    public void setHttpObjects(HttpServletRequest request, HttpServletResponse response) {
        if (this.request == null)
            this.request = request;

        if (this.response == null)
            this.response = response;
    }
}
