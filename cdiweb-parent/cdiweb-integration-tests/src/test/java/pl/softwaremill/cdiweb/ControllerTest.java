package pl.softwaremill.cdiweb;

import org.jboss.arquillian.testng.Arquillian;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pl.softwaremill.cdiweb.common.CDIWebContextProducer;
import pl.softwaremill.cdiweb.common.TestRecorder;
import pl.softwaremill.cdiweb.common.TestResourceResolver;
import pl.softwaremill.cdiweb.jaxrs.JAXPostHandler;
import pl.softwaremill.cdiweb.resource.ResourceResolver;
import pl.softwaremill.common.util.dependency.BeanManagerDependencyProvider;
import pl.softwaremill.common.util.dependency.D;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: szimano
 */
public class ControllerTest extends Arquillian {

    private BeanManagerDependencyProvider depProvider;

    @Inject
    private BeanManager bm;

    @Inject
    protected TestRecorder testRecorder;

    @Inject
    protected CDIWebContextProducer cdiWebContextProducer;

    protected HttpServletRequest req = mock(HttpServletRequest.class);
    protected HttpServletResponse resp = mock(HttpServletResponse.class);

    @BeforeMethod
    public void setup() {
        if (depProvider == null) {
            depProvider = new BeanManagerDependencyProvider(bm);
            D.register(depProvider);
        }
    }

    @AfterClass
    public void cleanup() {
        D.unregister(depProvider);
    }

    @AfterMethod
    public void clear() {
        testRecorder.clear();
        cdiWebContextProducer.clear();
    }

    protected JAXPostHandler getPostHandler() {
        ResourceResolver.Factory factory = mock(ResourceResolver.Factory.class);
        TestResourceResolver resourceResolver = new TestResourceResolver(req);
        when(factory.create((HttpServletRequest) anyObject())).thenReturn(resourceResolver);

        return new JAXPostHandler(factory);
    }

}
