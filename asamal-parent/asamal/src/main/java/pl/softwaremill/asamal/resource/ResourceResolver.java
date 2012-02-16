package pl.softwaremill.asamal.resource;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * Resource resolver used to resolve resources :)
 *
 * User: szimano
 */
public interface ResourceResolver {

    String ASAMAL_DEV_DIR = "ASAMAL_DEV_DIR";

    String resolveTemplate(String controller, String view) throws IOException;

    String resolvePartial(String controller, String partial) throws IOException;

    InputStream resolveFile(String path);

    interface Factory {
        ResourceResolver create(HttpServletRequest request);
    }
}
