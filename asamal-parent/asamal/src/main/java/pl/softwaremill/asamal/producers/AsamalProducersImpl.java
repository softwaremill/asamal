package pl.softwaremill.asamal.producers;

import pl.softwaremill.asamal.AsamalParameters;
import pl.softwaremill.asamal.controller.AsamalContext;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

@RequestScoped
public class AsamalProducersImpl implements AsamalProducers {

    private AsamalContext asamalContext;
    private AsamalParameters asamalParameters;

    @Produces
    @RequestScoped
    public AsamalContext produceAsamalContext() {
        return asamalContext;
    }

    @Produces
    @RequestScoped
    public AsamalParameters produceAsamalParamateres() {
        return asamalParameters;
    }

    public void setAsamalContext(AsamalContext asamalContext) {
        this.asamalContext = asamalContext;
    }

    public void setAsamalParameters(AsamalParameters asamalParameters) {
        this.asamalParameters = asamalParameters;
    }
}
