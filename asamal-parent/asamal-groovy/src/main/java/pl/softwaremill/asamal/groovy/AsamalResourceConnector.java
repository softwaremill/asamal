package pl.softwaremill.asamal.groovy;

import groovy.util.ResourceConnector;
import groovy.util.ResourceException;
import pl.softwaremill.asamal.extension.view.ResourceResolver;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * User: szimano
 */
public class AsamalResourceConnector implements ResourceConnector {

    private ServletContext servletContext;

    public AsamalResourceConnector(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public URLConnection getResourceConnection(String s) throws ResourceException {
        try {
            String path = "/groovy/" + s;

            if (System.getProperty(ResourceResolver.ASAMAL_DEV_DIR) != null) {
                return new URL("file", "", System.getProperty(ResourceResolver.ASAMAL_DEV_DIR) + path).openConnection();
            }
            else {
                return servletContext.getResource(path).openConnection();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cannot open groovy file", e);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open groovy file", e);
        }
    }
}
