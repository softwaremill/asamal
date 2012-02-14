package pl.softwaremill.cdiweb.common;

import pl.softwaremill.cdiweb.controller.CDIWebContext;
import pl.softwaremill.cdiweb.jaxrs.JAXPostHandler;

import javax.enterprise.inject.Produces;
import java.lang.reflect.Field;

/**
 * User: szimano
 */
public class CDIWebContextProducer {

    @Produces
    public CDIWebContext getContext() {
        return getCDIContextThreadLocal().get();
    }

    public void clear() {
        getCDIContextThreadLocal().remove();
    }

    private ThreadLocal<CDIWebContext> getCDIContextThreadLocal() {
        try {
            Field cdiWebContextHolder = JAXPostHandler.class.getDeclaredField("cdiWebContextHolder");
            cdiWebContextHolder.setAccessible(true);

            return ((ThreadLocal<CDIWebContext>) cdiWebContextHolder.get(null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
