package pl.softwaremill.asamal.get;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.softwaremill.asamal.ControllerTest;
import pl.softwaremill.asamal.MockAsamalParameters;
import pl.softwaremill.asamal.MockAsamalProducers;
import pl.softwaremill.asamal.common.TestRecorder;
import pl.softwaremill.asamal.common.TestResourceResolver;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.i18n.Messages;
import pl.softwaremill.asamal.jaxrs.JAXPostHandler;
import pl.softwaremill.asamal.resource.ResourceResolver;
import pl.softwaremill.common.util.dependency.D;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: szimano
 */
@RunWith(Arquillian.class)
public class GetControllerTest extends ControllerTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addClass(GetTestController.class)
                .addClass(ResourceResolver.Factory.class)
                .addClass(ResourceResolver.class)
                .addClass(TestNamedBean.class)
                .addClass(Messages.class)
                .addClass(MockAsamalProducers.class)
                .addPackage(TestResourceResolver.class.getPackage());
    }

    @Test
    public void shouldRunCorrectGetMethod() throws HttpErrorException {

        // given
        JAXPostHandler postHandler = getPostHandler();

        // when
        Response output = postHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("testMethod");
        assertThat(output.getEntity()).isEqualTo("<html>\n" +
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
        Response output = postHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("testMethod");
        Map viewHashMap = (Map) session.getAttribute(JAXPostHandler.VIEWHASH_MAP);

        // check that only one viewHash was created
        assertThat(viewHashMap.keySet()).hasSize(1);

        assertThat(output.getEntity()).isEqualTo("<html>\n" +
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
        Response output = postHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(output.getEntity().toString()).contains("Some Value From Named Bean");
    }

    @Test
    public void shouldUseI18nProperlyInDefault() throws HttpErrorException {
        // given
        JAXPostHandler postHandler = getPostHandler();
        TestResourceResolver.returnHtml =
                "<html><body>" +
                        "$m['helo']" +
                        "</body></html>";
        Locale.setDefault(Locale.ENGLISH);

        // when
        Response output = postHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(output.getEntity().toString()).contains("Helo!");
    }

    @Test
    public void shouldUseI18nProperlyInSpecificLocale() throws HttpErrorException {
        // given
        JAXPostHandler postHandler = getPostHandler();
        TestResourceResolver.returnHtml =
                "<html><body>" +
                        "$m['helo']" +
                        "</body></html>";
        Locale.setDefault(Locale.GERMAN);

        // when
        Response output = postHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(output.getEntity().toString()).contains("Guten Morgen!");
    }

    @Test
    public void shouldPassAnnotatedParams() throws HttpErrorException {
        // given
        JAXPostHandler postHandler = getPostHandler();
        producers.setMockAsamalParameters(new MockAsamalParameters(new HashMap<String, Object>(){{
            put("param", "testParamContent");
        }}));

        // when
        Response output = postHandler.handleGet(req, resp, "get", "testWithParams", null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("testWithParams_testParamContent");
    }

    @Test
    public void shouldPassAnnotatedObjectParams() throws HttpErrorException {
        // given
        JAXPostHandler postHandler = getPostHandler();
        producers.setMockAsamalParameters(new MockAsamalParameters(new HashMap<String, Object>(){{
            put("param", new Object[]{"This", "is", "it"});
        }}));

        // when
        Response output = postHandler.handleGet(req, resp, "get", "testWithObjectParamsRequired", null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled())
                .containsOnly("testWithObjectParamsRequired_[This, is, it]");
    }

    @Test
    public void shouldReturnNullOnNonExistingAnnotatedParam() throws HttpErrorException {
        // given
        JAXPostHandler postHandler = getPostHandler();

        // when
        Response output = postHandler.handleGet(req, resp, "get", "testWithParams", null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("testWithParams_null");
    }

    @Test(expected = HttpErrorException.class)
    public void shouldFailIfNoRequiredParam() throws HttpErrorException {
        // given
        JAXPostHandler postHandler = getPostHandler();

        // when
        Response output = postHandler.handleGet(req, resp, "get", "testWithParamsRequired", null);
    }

    @Test(expected = HttpErrorException.class)
    public void shouldFailIfNoRequiredObjectParam() throws HttpErrorException {
        // given
        JAXPostHandler postHandler = getPostHandler();

        // when
        Response output = postHandler.handleGet(req, resp, "get", "testWithObjectParamsRequired", null);
    }

}