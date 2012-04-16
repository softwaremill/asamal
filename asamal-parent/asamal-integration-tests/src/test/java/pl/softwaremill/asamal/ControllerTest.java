package pl.softwaremill.asamal;

import org.jboss.arquillian.container.weld.ee.embedded_1_1.mock.MockHttpSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import pl.softwaremill.asamal.common.TestRecorder;
import pl.softwaremill.asamal.common.TestResourceResolver;
import pl.softwaremill.asamal.controller.cdi.AsamalAnnotationScanner;
import pl.softwaremill.asamal.extension.view.ResourceResolver;
import pl.softwaremill.asamal.httphandler.AsamalViewHandler;
import pl.softwaremill.asamal.httphandler.GetHandler;
import pl.softwaremill.asamal.httphandler.PostHandler;
import pl.softwaremill.asamal.servlet.AsamalListener;
import pl.softwaremill.asamal.viewhash.ViewDescriptor;
import pl.softwaremill.asamal.viewhash.ViewHashGenerator;
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
    protected AsamalAnnotationScanner asamalAnnotationScanner;

    @Inject
    protected ViewHashGenerator viewHashGenerator;

    @Inject
    protected AsamalViewHandler viewHandler;

    @Inject
    protected MockAsamalProducers producers;

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

        // this is used to resolve BM by the PostHandler
        when(servletContext.getAttribute(AsamalListener.BEAN_MANAGER)).thenReturn(bm);
    }

    @After
    public void cleanup() {
        D.unregister(depProvider);
    }

    @After
    public void clear() {
        TestResourceResolver.returnHtml = null;
        testRecorder.clear();
        producers.clear();
    }

    protected PostHandler getPostHandler() {
        return new PostHandler(producers, viewHashGenerator, viewHandler);
    }

    protected GetHandler getGetHandler() {
        ResourceResolver.Factory factory = mock(ResourceResolver.Factory.class);
        TestResourceResolver resourceResolver = new TestResourceResolver(req);
        when(factory.create((HttpServletRequest) anyObject())).thenReturn(resourceResolver);

        return new GetHandler(producers, viewHandler, factory);
    }

    protected void addViewHash(String viewHash, String controller, String view) {
        Map<String, ViewDescriptor> map = (Map<String, ViewDescriptor>) session.getAttribute(ViewHashGenerator.VIEWHASH_MAP);
        
        if (map == null) {
            session.setAttribute(ViewHashGenerator.VIEWHASH_MAP, map = new HashMap<String, ViewDescriptor>());
        }

        map.put(viewHash, new ViewDescriptor(controller, view));
    }

}
