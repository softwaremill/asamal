package pl.softwaremill.asamal;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.producers.AsamalProducers;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

@RequestScoped
public class MockAsamalProducers implements AsamalProducers {

    private AsamalContext asamalContext;
    private AsamalParameters asamalParameters;

    private AsamalContext mockAsamalContext;
    private AsamalParameters mockAsamalParameters;

    @Override
    public void setAsamalContext(AsamalContext asamalContext) {
        this.asamalContext = asamalContext;
    }

    @Override
    public void setAsamalParameters(AsamalParameters asamalParameters) {
        this.asamalParameters = asamalParameters;
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

    private <T> T resolveMock(T mock, T original) {
        return (mock != null) ? mock : original;
    }

    public void clear() {
        mockAsamalContext = asamalContext = null;
        asamalParameters = mockAsamalParameters = null;
    }
}
