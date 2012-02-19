package pl.softwaremill.asamal;

import org.jboss.arquillian.container.weld.ee.embedded_1_1.mock.MockHttpSession;
import org.jboss.arquillian.testng.Arquillian;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pl.softwaremill.asamal.common.AsamalContextProducer;
import pl.softwaremill.asamal.common.TestRecorder;
import pl.softwaremill.asamal.common.TestResourceResolver;
import pl.softwaremill.asamal.jaxrs.JAXPostHandler;
import pl.softwaremill.asamal.resource.ResourceResolver;
import pl.softwaremill.asamal.servlet.AsamalListener;
import pl.softwaremill.asamal.viewhash.ViewDescriptor;
import pl.softwaremill.common.util.dependency.BeanManagerDependencyProvider;
import pl.softwaremill.common.util.dependency.D;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
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
    protected AsamalContextProducer asamalContextProducer;

    protected HttpServletRequest req = mock(HttpServletRequest.class);
    protected HttpServletResponse resp = mock(HttpServletResponse.class);

    protected HttpSession session;
    protected ServletContext servletContext = mock(ServletContext.class);
    

    @BeforeMethod
    public void setup() {
        if (depProvider == null) {
            depProvider = new BeanManagerDependencyProvider(bm);
            D.register(depProvider);
        }
        
        session = new MockHttpSession();

        when(req.getSession()).thenReturn(session);
        when(req.getServletContext()).thenReturn(servletContext);

        // this is used to resolve BM by the JAXPostHandler
        when(servletContext.getAttribute(AsamalListener.BEAN_MANAGER)).thenReturn(bm);
    }

    @AfterClass
    public void cleanup() {
        D.unregister(depProvider);
    }

    @AfterMethod
    public void clear() {
        testRecorder.clear();
        asamalContextProducer.clear();
    }

    protected JAXPostHandler getPostHandler() {
        ResourceResolver.Factory factory = mock(ResourceResolver.Factory.class);
        TestResourceResolver resourceResolver = new TestResourceResolver(req);
        when(factory.create((HttpServletRequest) anyObject())).thenReturn(resourceResolver);

        return new JAXPostHandler(factory);
    }

    protected void addViewHash(String viewHash, String controller, String view) {
        Map<String, ViewDescriptor> map = (Map<String, ViewDescriptor>) session.getAttribute(JAXPostHandler.VIEWHASH_MAP);
        
        if (map == null) {
            session.setAttribute(JAXPostHandler.VIEWHASH_MAP, map = new HashMap<String, ViewDescriptor>());
        }

        map.put(viewHash, new ViewDescriptor(controller, view));
    }

}
