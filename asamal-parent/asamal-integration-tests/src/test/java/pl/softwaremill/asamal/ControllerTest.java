package pl.softwaremill.asamal;

import org.apache.struts.mock.MockHttpSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import pl.softwaremill.asamal.common.AsamalContextProducer;
import pl.softwaremill.asamal.common.TestRecorder;
import pl.softwaremill.asamal.common.TestResourceResolver;
import pl.softwaremill.asamal.controller.cdi.BootstrapCheckerExtension;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: szimano
 */
@Ignore
public class ControllerTest {

    private BeanManagerDependencyProvider depProvider;

    @Inject
    private BeanManager bm;

    @Inject
    protected TestRecorder testRecorder;

    @Inject
    protected AsamalContextProducer asamalContextProducer;

    @Inject
    protected BootstrapCheckerExtension bootstrapCheckerExtension;

    protected HttpServletRequest req = mock(HttpServletRequest.class);
    protected HttpServletResponse resp = mock(HttpServletResponse.class);

    protected HttpSession session;
    protected ServletContext servletContext = mock(ServletContext.class);
    

    @Before
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

    @After
    public void cleanup() {
        D.unregister(depProvider);
    }

    @After
    public void clear() {
        testRecorder.clear();
        asamalContextProducer.clear();
    }

    protected JAXPostHandler getPostHandler() {
        ResourceResolver.Factory factory = mock(ResourceResolver.Factory.class);
        TestResourceResolver resourceResolver = new TestResourceResolver(req);
        when(factory.create((HttpServletRequest) anyObject())).thenReturn(resourceResolver);

        return new JAXPostHandler(factory, bootstrapCheckerExtension);
    }

    protected void addViewHash(String viewHash, String controller, String view) {
        Map<String, ViewDescriptor> map = (Map<String, ViewDescriptor>) session.getAttribute(JAXPostHandler.VIEWHASH_MAP);
        
        if (map == null) {
            session.setAttribute(JAXPostHandler.VIEWHASH_MAP, map = new HashMap<String, ViewDescriptor>());
        }

        map.put(viewHash, new ViewDescriptor(controller, view));
    }

}
