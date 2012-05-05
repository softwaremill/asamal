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
import pl.softwaremill.asamal.controller.cdi.AsamalAnnotationScanner;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.extension.view.PresentationExtensionResolver;
import pl.softwaremill.asamal.extension.view.ResourceResolver;
import pl.softwaremill.asamal.helper.AsamalHelper;
import pl.softwaremill.asamal.httphandler.AsamalViewHandler;
import pl.softwaremill.asamal.httphandler.GetHandler;
import pl.softwaremill.asamal.i18n.Messages;
import pl.softwaremill.asamal.plugin.velocity.AsamalVelocityExtension;
import pl.softwaremill.asamal.plugin.velocity.context.VelocityPresentationContext;
import pl.softwaremill.asamal.viewhash.ViewHashGenerator;
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
                .addClass(ViewHashGenerator.class)
                .addClass(AsamalViewHandler.class)
                .addClass(AsamalHelper.class)
                .addClass(VelocityPresentationContext.class)
                .addClass(PresentationExtensionResolver.class)
                .addClass(AsamalVelocityExtension.class)
                .addPackage(TestResourceResolver.class.getPackage())
                .addAsServiceProviderAndClasses(AsamalAnnotationScanner.class);
    }

    @Test
    public void shouldRunCorrectGetMethod() throws HttpErrorException {

        // given
        GetHandler getHandler = getGetHandler();

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("testMethod");
        assertThat(output.getEntity()).isEqualTo("<html>\n" +
                " <head>\n" +
                "  <script type=\"text/javascript\" src=\"//asamal/asamal.js\"></script>\n" +
                "  <script type=\"text/javascript\">var asamalController = 'get'; var asamalView = 'testMethod'; var asamalViewHash = '" +
                viewHashGenerator.getViewHashMap().keySet().iterator().next() + "'; var asamalContextPath = '/'; </script>\n" +
                " </head>\n" +
                " <body>\n" +
                "  get/testMethod\n" +
                " </body>\n" +
                "</html>");
    }

    @Test
    public void shouldAddHiddenTypeWithViewHashAndAsamalJS() throws HttpErrorException {

        // given
        GetHandler getHandler = getGetHandler();
        TestResourceResolver.returnHtml =
                "<html><body>" +
                        "<form method='POST' action='action' accept-charset='UTF-8'>" +
                        "<input type='text'/>" +
                        "</form>" +
                        "</body></html>";

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("testMethod");
        Map viewHashMap = (Map) session.getAttribute(ViewHashGenerator.VIEWHASH_MAP);

        // check that only one viewHash was created
        assertThat(viewHashMap.keySet()).hasSize(1);

        assertThat(output.getEntity()).isEqualTo("<html>\n" +
                " <head>\n" +
                "  <script type=\"text/javascript\" src=\"//asamal/asamal.js\"></script>\n" +
                "  <script type=\"text/javascript\">var asamalController = 'get'; var asamalView = 'testMethod'; var asamalViewHash = '" +
                viewHashGenerator.getViewHashMap().keySet().iterator().next() + "'; var asamalContextPath = '/'; </script>\n" +
                " </head>\n" +
                " <body>\n" +
                "  <form method=\"POST\" action=\"action\" accept-charset=\"UTF-8\">\n" +
                "   <input type=\"text\" />\n" +
                "   <input type=\"hidden\" name=\"asamalViewHash\" value=\"" +
                viewHashMap.keySet().iterator().next() + "\" />\n" +
                "  </form>\n" +
                " </body>\n" +
                "</html>");
    }

    @Test
    public void shouldIncludeNamedBeansInContext() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();
        TestResourceResolver.returnHtml =
                "<html><body>" +
                        "$testNamedBean.value" +
                        "</body></html>";

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(output.getEntity().toString()).contains("Some Value From Named Bean");
    }

    @Test
    public void shouldUseI18nProperlyInDefault() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();
        TestResourceResolver.returnHtml =
                "<html><body>" +
                        "$m['helo']" +
                        "</body></html>";
        Locale.setDefault(Locale.ENGLISH);

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(output.getEntity().toString()).contains("Helo!");
    }

    @Test
    public void shouldUseI18nProperlyInSpecificLocale() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();
        TestResourceResolver.returnHtml =
                "<html><body>" +
                        "$m['helo']" +
                        "</body></html>";
        Locale.setDefault(Locale.GERMAN);

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testMethod", null);

        // then
        assertThat(output.getEntity().toString()).contains("Guten Morgen!");
    }

    @Test
    public void shouldPassAnnotatedParams() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();
        producers.setMockAsamalParameters(new MockAsamalParameters(new HashMap<String, Object>() {{
            put("param", "testParamContent");
        }}));

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testWithParams", null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("testWithParams_testParamContent");
    }

    @Test
    public void shouldPassAnnotatedObjectParams() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();
        producers.setMockAsamalParameters(new MockAsamalParameters(new HashMap<String, Object>() {{
            put("param", new Object[]{"This", "is", "it"});
        }}));

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testWithObjectParamsRequired", null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled())
                .containsOnly("testWithObjectParamsRequired_[This, is, it]");
    }

    @Test
    public void shouldReturnNullOnNonExistingAnnotatedParam() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testWithParams", null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("testWithParams_null");
    }

    @Test(expected = HttpErrorException.class)
    public void shouldFailIfNoRequiredParam() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testWithParamsRequired", null);
    }

    @Test(expected = HttpErrorException.class)
    public void shouldFailIfNoRequiredObjectParam() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testWithObjectParamsRequired", null);
    }

    @Test
    public void shouldPassOnProperPathParams() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testWithPathParams", "23/Tomek");

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly(
                "testWithPathParams id = 23 and name = Tomek");
    }

    @Test(expected = HttpErrorException.class)
    public void shouldFailWhenNotEnoughParams() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testWithPathParams", "23");
    }

    @Test
    public void shouldPassWhenMoreParams() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testWithPathParams", "10/Domek/23/45/667/ee");

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly(
                "testWithPathParams id = 10 and name = Domek");
    }

    @Test(expected = HttpErrorException.class)
    public void shouldFailWhenParamNotDefined() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testWithPathParamsWrong", "123");
    }

    @Test
    public void shouldPassMixedAnnotatedParams() throws HttpErrorException {
        // given
        GetHandler getHandler = getGetHandler();
        producers.setMockAsamalParameters(new MockAsamalParameters(new HashMap<String, Object>() {{
            put("param", "testParamContent");
        }}));

        // when
        Response output = getHandler.handleGet(req, resp, "get", "testWithPathParamsMixed", "17");

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).
                containsOnly("testWithPathParamsMixed id = 17 param = testParamContent");
    }

}