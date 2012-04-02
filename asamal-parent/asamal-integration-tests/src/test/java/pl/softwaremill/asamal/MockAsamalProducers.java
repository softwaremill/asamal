package pl.softwaremill.asamal;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.producers.AsamalProducers;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestScoped
public class MockAsamalProducers implements AsamalProducers {

    private AsamalContext asamalContext;
    private AsamalParameters asamalParameters;

    private AsamalContext mockAsamalContext;
    private AsamalParameters mockAsamalParameters;

    private HttpServletRequest request;
    private HttpServletResponse response;

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;

    public void setAsamalContext(AsamalContext asamalContext) {
        this.asamalContext = asamalContext;
    }

    public void setAsamalParameters(AsamalParameters asamalParameters) {
        this.asamalParameters = asamalParameters;
    }

    public void setMockRequest(HttpServletRequest mockRequest) {
        this.mockRequest = mockRequest;
    }

    public void setMockResponse(HttpServletResponse mockResponse) {
        this.mockResponse = mockResponse;
    }

    public void setMockAsamalContext(AsamalContext mockAsamalContext) {
        this.mockAsamalContext = mockAsamalContext;
    }

    public void setMockAsamalParameters(AsamalParameters mockAsamalParameters) {
        this.mockAsamalParameters = mockAsamalParameters;
    }

    @Produces
    public AsamalContext produceContext() {
        return resolveMock(mockAsamalContext, asamalContext);
    }

    @Produces
    public AsamalParameters produceParameters() {
        return resolveMock(mockAsamalParameters, asamalParameters);
    }

    @Produces
    public HttpServletRequest produceRequest() {
        return resolveMock(mockRequest, request);
    }

    @Produces
    public HttpServletResponse produceResponse() {
        return resolveMock(mockResponse, response);
    }

    private <T> T resolveMock(T mock, T original) {
        return (mock != null) ? mock : original;
    }

    public void clear() {
        asamalContext = mockAsamalContext = null;
        asamalParameters = mockAsamalParameters = null;
        request = mockRequest = null;
        response = mockResponse = null;
    }

    public void setHttpObjects(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }
}
