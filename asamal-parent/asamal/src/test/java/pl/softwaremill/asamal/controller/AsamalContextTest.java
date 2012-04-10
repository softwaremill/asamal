package pl.softwaremill.asamal.controller;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class AsamalContextTest {

    @Test
    public void testRedirects() throws IOException {
        // given

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getContextPath()).thenReturn("/test");

        AsamalContext ac = new AsamalContext(request, response, null);

        // when

        ac.redirect("controller", "view", null);

        // then
        verify(response).sendRedirect("/test/controller/view");
    }

    @Test
    public void testRedirectsWithOneParam() throws IOException {
        // given

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getContextPath()).thenReturn("/test");

        AsamalContext ac = new AsamalContext(request, response, null);

        // when

        ac.redirect("controller", "view", new PageParameters(1l));

        // then
        verify(response).sendRedirect("/test/controller/view/1");
    }

    @Test
    public void testRedirectsWithMoreParams() throws IOException {
        // given

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getContextPath()).thenReturn("/test");

        AsamalContext ac = new AsamalContext(request, response, null);

        // when

        ac.redirect("controller", "view", new PageParameters(1l, "This", "ża"));

        // then
        verify(response).sendRedirect("/test/controller/view/1/This/%C5%BCa");
    }

    @Test
    public void testRedirectsWithEmptyParams() throws IOException {
        // given

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getContextPath()).thenReturn("/test");

        AsamalContext ac = new AsamalContext(request, response, null);

        // when

        ac.redirect("controller", "view", new PageParameters());

        // then
        verify(response).sendRedirect("/test/controller/view");
    }
}
