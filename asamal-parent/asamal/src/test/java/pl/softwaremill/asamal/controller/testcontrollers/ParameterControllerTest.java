package pl.softwaremill.asamal.controller.testcontrollers;

import org.junit.Test;
import pl.softwaremill.asamal.AsamalParameters;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: szimano
 */
public class ParameterControllerTest extends ControllerBean {

    @Test
    public void shouldReturnNullForNonExistingParamsFromRequest() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(new HashMap<String, String[]>());
        context = new AsamalContext(request, mock(HttpServletResponse.class), null);
        setParameters(new AsamalParameters(request, null));

        // when
        String parameter = getParameter("non-existing");
        List<String> parameterValues = getParameterValues("non-existing");

        // then
        assertThat(parameter).isNull();
        assertThat(parameterValues).isNull();
    }

    @Test
    public void shouldReturnNullForNonExistingParamsFromParamMap() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        context = new AsamalContext(request, mock(HttpServletResponse.class), null);
        setParameters(new AsamalParameters(request, null));

        // when
        String parameter = getParameter("non-existing");
        List<String> parameterValues = getParameterValues("non-existing");

        // then
        assertThat(parameter).isNull();
        assertThat(parameterValues).isNull();
    }

}
