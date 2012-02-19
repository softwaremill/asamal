package pl.softwaremill.asamal.get;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;
import pl.softwaremill.asamal.ControllerTest;
import pl.softwaremill.asamal.common.TestRecorder;
import pl.softwaremill.asamal.common.TestResourceResolver;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.jaxrs.JAXPostHandler;
import pl.softwaremill.asamal.resource.ResourceResolver;
import pl.softwaremill.common.util.dependency.D;

import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: szimano
 */
public class GetControllerTest extends ControllerTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addClass(GetTestController.class)
                .addClass(ResourceResolver.Factory.class)
                .addClass(ResourceResolver.class)
                .addClass(TestNamedBean.class)
                .addPackage(TestResourceResolver.class.getPackage());
    }

    @Test
    public void shouldRunCorrectGetMethod() throws HttpErrorException {

        // given
        JAXPostHandler postHandler = getPostHandler();

        // when
        String output = postHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("testMethod");
        assertThat(output).isEqualTo("<html>\n" +
                " <head>\n" +
                "  <script type=\"text/javascript\" src=\"null/asamal/asamal.js\"></script>\n" +
                " </head>\n" +
                " <body>\n" +
                "  get/testMethod\n" +
                " </body>\n" +
                "</html>");
    }

    @Test
    public void shouldAddHiddenTypeWithViewHashAndAsamalJS() throws HttpErrorException {

        // given
        JAXPostHandler postHandler = getPostHandler();
        TestResourceResolver.returnHtml =
                "<html><body>" +
                        "<form method='POST' action='action'>" +
                        "<input type='text'/>" +
                        "</form>" +
                        "</body></html>";

        // when
        String output = postHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("testMethod");
        Map viewHashMap = (Map) session.getAttribute(JAXPostHandler.VIEWHASH_MAP);

        // check that only one viewHash was created
        assertThat(viewHashMap.keySet()).hasSize(1);

        assertThat(output).isEqualTo("<html>\n" +
                " <head>\n" +
                "  <script type=\"text/javascript\" src=\"null/asamal/asamal.js\"></script>\n" +
                " </head>\n" +
                " <body>\n" +
                "  <form method=\"POST\" action=\"action\">\n" +
                "   <input type=\"text\" />\n" +
                "   <input type=\"hidden\" name=\"asamalViewHash\" value=\""+
                    viewHashMap.keySet().iterator().next() +"\" />\n" +
                "  </form>\n" +
                " </body>\n" +
                "</html>");
    }

    @Test
    public void shouldIncludeNamedBeansInContext() throws HttpErrorException {
        // given
        JAXPostHandler postHandler = getPostHandler();
        TestResourceResolver.returnHtml =
                "<html><body>" +
                        "$testNamedBean.value" +
                        "</body></html>";

        // when
        String output = postHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(output).contains("Some Value From Named Bean");
    }
}