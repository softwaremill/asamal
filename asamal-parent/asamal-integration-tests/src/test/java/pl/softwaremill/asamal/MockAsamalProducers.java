package pl.softwaremill.asamal;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.producers.AsamalProducers;
import pl.softwaremill.common.util.RichString;

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
        return mockRequest;
    }

    @Produces
    public HttpServletResponse produceResponse() {
        return mockResponse;
    }

    private <T> T resolveMock(T mock, T original) {
        return (mock != null) ? mock : original;
    }

    public void clear() {
        asamalContext = mockAsamalContext = null;
        asamalParameters = mockAsamalParameters = null;
        mockRequest = null;
        mockResponse = null;
    }

    public static void main(String[] args) {
        System.out.println(new RichString("Tomeczek").encodeAsPassword());
    }
}
