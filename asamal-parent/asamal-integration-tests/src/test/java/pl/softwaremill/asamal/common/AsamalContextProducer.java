package pl.softwaremill.asamal.common;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.jaxrs.JAXPostHandler;

import javax.enterprise.inject.Produces;
import java.lang.reflect.Field;

/**
 * User: szimano
 */
public class AsamalContextProducer {

    @Produces
    public AsamalContext getContext() {
        return getCDIContextThreadLocal().get();
    }

    public void clear() {
        getCDIContextThreadLocal().remove();
    }

    private ThreadLocal<AsamalContext> getCDIContextThreadLocal() {
        try {
            Field asamalContextHolder = JAXPostHandler.class.getDeclaredField("asamalContextHolder");
            asamalContextHolder.setAccessible(true);

            return ((ThreadLocal<AsamalContext>) asamalContextHolder.get(null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
