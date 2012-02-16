package pl.softwaremill.asamal.get;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;
import pl.softwaremill.asamal.ControllerTest;
import pl.softwaremill.asamal.common.TestRecorder;
import pl.softwaremill.asamal.common.TestResourceResolver;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.jaxrs.JAXPostHandler;
import pl.softwaremill.asamal.resource.ResourceResolver;
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