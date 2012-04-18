package pl.softwaremill.asamal.httphandler;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.cdi.AsamalAnnotationScanner;
import pl.softwaremill.asamal.extension.view.PresentationExtensionResolver;
import pl.softwaremill.asamal.extension.view.ResourceResolver;
import pl.softwaremill.asamal.plugin.velocity.AsamalVelocityExtension;
import pl.softwaremill.asamal.resource.ResourceResolverImpl;
import pl.softwaremill.asamal.viewhash.ViewHashGenerator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AsamalViewHandlerTest {

    HttpServletRequest request;
    ControllerBean controller;
    AsamalViewHandler viewHandler;

    @Test
    public void shouldPreserveTextAreaWhitespace() throws Exception {
        // given
        setupMocks(WhiteSpaceController.class);

        // when
        String view = viewHandler.showView(request, controller, "textarea", "whitespace");

        // then
        assertThat(view).isEqualTo("<html>\n" +
                " <head>\n" +
                "  <script type=\"text/javascript\" src=\"//asamal/asamal.js\"></script>\n" +
                "  <script type=\"text/javascript\">var asamalController = 'textarea'; var asamalView = 'whitespace'; var asamalViewHash = 'viewhash'; var asamalContextPath = '/'; </script>\n" +
                " </head>\n" +
                " <body>\n" +
                "  <textarea>\n" +
                "    This\n" +
                "    is\n" +
                "    whitespaced\n" +
                "    text\n" +
                "</textarea>\n" +
                " </body>\n" +
                "</html>");
    }

    @Test
    public void shouldPreserveHtml() throws Exception {
        // given
        setupMocks(WhiteSpaceController.class);

        // when
        String view = viewHandler.showView(request, controller, "textarea", "html");

        // then
        assertThat(view).isEqualTo("<html>\n" +
                " <head>\n" +
                "  <script type=\"text/javascript\" src=\"//asamal/asamal.js\"></script>\n" +
                "  <script type=\"text/javascript\">var asamalController = 'textarea'; var asamalView = 'html'; var asamalViewHash = 'viewhash'; var asamalContextPath = '/'; </script>\n" +
                " </head>\n" +
                " <body>\n" +
                "  <textarea>\n" +
                StringEscapeUtils.escapeHtml("    <script type=\"text/javascript\">alert(\"Test\")</script>") +
                "\n</textarea>\n" +
                " </body>\n" +
                "</html>");
    }

    @After
    public void cleanupMocks() {
        request = null;
        controller = null;
        viewHandler = null;
    }

    private void setupMocks(Class<? extends ControllerBean> controllerBeanClass) throws Exception {
        AsamalAnnotationScanner checkerExtension = mock(AsamalAnnotationScanner.class);

        ViewHashGenerator viewHashGenerator = mock(ViewHashGenerator.class);
        when(viewHashGenerator.createNewViewHash(anyString(), anyString())).thenReturn("viewhash");

        ResourceResolver.Factory resourceResolverFactory = new ResourceResolver.Factory(){
            @Override
            public ResourceResolver create(HttpServletRequest request) {
                return new ResourceResolverImpl(request);
            }
        };

        PresentationExtensionResolver presentationExtensionResolver = mock(PresentationExtensionResolver.class);
        when(presentationExtensionResolver.resolvePresentationExtension((ResourceResolver) anyObject(),
                anyString(), anyString())).thenReturn(new AsamalVelocityExtension());

        viewHandler = new AsamalViewHandler(checkerExtension, viewHashGenerator,
                resourceResolverFactory, presentationExtensionResolver);
        controller = controllerBeanClass.newInstance();
        request = mock(HttpServletRequest.class);
        ServletContext context = mock(ServletContext.class);
        when(request.getServletContext()).thenReturn(context);
        when(request.getContextPath()).thenReturn("/");
        when(context.getResourceAsStream(anyString())).then(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                return AsamalViewHandler.class.getResourceAsStream((String) invocation.getArguments()[0]);
            }
        });
    }
}
