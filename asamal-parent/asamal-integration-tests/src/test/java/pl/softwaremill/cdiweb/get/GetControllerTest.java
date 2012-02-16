package pl.softwaremill.cdiweb.get;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;
import pl.softwaremill.cdiweb.ControllerTest;
import pl.softwaremill.cdiweb.common.TestRecorder;
import pl.softwaremill.cdiweb.common.TestResourceResolver;
import pl.softwaremill.cdiweb.exception.HttpErrorException;
import pl.softwaremill.cdiweb.jaxrs.JAXPostHandler;
import pl.softwaremill.cdiweb.resource.ResourceResolver;
import pl.softwaremill.common.util.dependency.D;

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
        assertThat(output).isEqualTo("get/testMethod");
    }
}