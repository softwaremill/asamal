package pl.softwaremill.asamal.controller.testcontrollers;

import org.junit.After;
import org.junit.Test;
import pl.softwaremill.asamal.AsamalParameters;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.exception.AutobindingException;
import pl.softwaremill.asamal.controller.exception.NoSuchParameterException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: szimano
 */
public class AutoBindControllerTest extends ControllerBean {

    private TestPojo testPojo;

    public TestPojo getTestPojo() {
        return testPojo;
    }

    public void setTestPojo(TestPojo testPojo) {
        this.testPojo = testPojo;
    }

    @After
    public void cleanUp() {
        testPojo = null;
    }
    
    @Test
    public void shouldAutobindSuccessfully() {
        // given
        testPojo = new TestPojo();

        AsamalParameters parameters = mock(AsamalParameters.class);
        when(parameters.getObjectParameterValues("testPojo.test")).thenReturn(
                Collections.singletonList((Object) "testPojo"));
        when(parameters.getObjectParameterValues("testPojo.inTest.intest"))
                .thenReturn(Collections.singletonList((Object) "intest"));
        when(parameters.getParameterNames()).thenReturn(new HashSet<String>(Arrays.asList("testPojo.test",
                "testPojo.inTest.intest")));
        setParameters(parameters);

        // when
        doAutoBinding("testPojo.test", "testPojo.inTest.intest");

        // then
        assertThat(testPojo.getTest()).isEqualTo("testPojo");
        assertThat(testPojo.getInTest().getIntest()).isEqualTo("intest");
    }

    @Test(expected = NoSuchParameterException.class)
    public void shouldShowCorrectMessageOnNonExistingParameter() {
        // given
        testPojo = new TestPojo();

        AsamalContext context = mock(AsamalContext.class);
        setContext(context);

        // when
        doAutoBinding("testPojo.test");
    }

    @Test
    public void shouldAutobindFineIfParameterValueIsEmpty() {
        // given
        testPojo = new TestPojo();

        AsamalParameters parameters = mock(AsamalParameters.class);
        when(parameters.getParameterValues("testPojo.test")).thenReturn(null);
        when(parameters.getParameterNames()).thenReturn(new HashSet<String>(Arrays.asList("testPojo.test")));
        setParameters(parameters);

        // when
        doAutoBinding("testPojo.test");

        // then
        assertThat(testPojo.getTest()).isNull();
    }

    @Test(expected = AutobindingException.class)
    public void shouldThrowExceptionWhenBeanNull() {
        // given
        AsamalParameters parameters = mock(AsamalParameters.class);
        when(parameters.getParameterValues("testPojo.test")).thenReturn(Collections.singletonList("testPojo"));
        when(parameters.getParameterNames()).thenReturn(new HashSet<String>(Arrays.asList("testPojo.test")));
        setParameters(parameters);

        // when
        doAutoBinding("testPojo.test");
    }

}
