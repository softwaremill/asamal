package pl.softwaremill.asamal.producers;

import pl.softwaremill.asamal.AsamalParameters;
import pl.softwaremill.asamal.controller.AsamalContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for producing different Asamal objects in CDI
 */
public interface AsamalProducers {

    void setAsamalContext(AsamalContext asamalContext);

    void setAsamalParameters(AsamalParameters asamalParameters);

    void setHttpObjects(HttpServletRequest request, HttpServletResponse response);

}
