package pl.softwaremill.asamal.controller.testcontrollers;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
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

    @AfterMethod
    public void cleanUp() {
        testPojo = null;
    }
    
    @Test
    public void shouldAutobindSuccessfully() {
        // given
        testPojo = new TestPojo();

        AsamalContext context = mock(AsamalContext.class);
        when(context.getObjectParameterValues("testPojo.test")).thenReturn(
                Collections.singletonList((Object)"testPojo"));
        when(context.getObjectParameterValues("testPojo.inTest.intest"))
                .thenReturn(Collections.singletonList((Object)"intest"));
        when(context.getParameterNames()).thenReturn(new HashSet<String>(Arrays.asList("testPojo.test",
                "testPojo.inTest.intest")));
        setContext(context);

        // when
        doAutoBinding("testPojo.test", "testPojo.inTest.intest");

        // then
        assertThat(testPojo.getTest()).isEqualTo("testPojo");
        assertThat(testPojo.getInTest().getIntest()).isEqualTo("intest");
    }

    @Test(expectedExceptions = NoSuchParameterException.class)
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

        AsamalContext context = mock(AsamalContext.class);
        when(context.getParameterValues("testPojo.test")).thenReturn(null);
        when(context.getParameterNames()).thenReturn(new HashSet<String>(Arrays.asList("testPojo.test")));
        setContext(context);

        // when
        doAutoBinding("testPojo.test");

        // then
        assertThat(testPojo.getTest()).isNull();
    }

    @Test(expectedExceptions = AutobindingException.class)
    public void shouldThrowExceptionWhenBeanNull() {
        // given
        AsamalContext context = mock(AsamalContext.class);
        when(context.getParameterValues("testPojo.test")).thenReturn(Collections.singletonList("testPojo"));
        when(context.getParameterNames()).thenReturn(new HashSet<String>(Arrays.asList("testPojo.test")));
        setContext(context);

        // when
        doAutoBinding("testPojo.test");
    }

}
