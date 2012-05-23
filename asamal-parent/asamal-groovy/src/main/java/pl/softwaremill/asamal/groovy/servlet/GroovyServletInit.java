package pl.softwaremill.asamal.groovy.servlet;

import pl.softwaremill.asamal.groovy.AsamalResourceConnector;
import pl.softwaremill.asamal.groovy.GroovyResourceResolver;
import pl.softwaremill.common.util.dependency.D;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * User: szimano
 */
@WebListener
public class GroovyServletInit implements ServletContextListener{

    private GroovyResourceResolver grr;

    public void contextInitialized(ServletContextEvent sce) {
        grr = new GroovyResourceResolver(new AsamalResourceConnector(sce.getServletContext()));

        D.register(grr);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        D.unregister(grr);
    }
}
