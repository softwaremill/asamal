package pl.softwaremill.asamal.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Sets the proper character encoding on the http request and response
 */
@WebFilter(urlPatterns = "/*")
public class UTF8HttpFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @SuppressWarnings("unchecked")
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        request.setCharacterEncoding("UTF8");
        response.setCharacterEncoding("UTF8");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
