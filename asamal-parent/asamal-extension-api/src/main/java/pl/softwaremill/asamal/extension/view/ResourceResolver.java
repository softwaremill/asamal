package pl.softwaremill.asamal.extension.view;

import pl.softwaremill.asamal.extension.exception.ResourceNotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * Resource resolver used to resolve resources :)
 *
 * User: szimano
 */
public interface ResourceResolver {

    String ASAMAL_DEV_DIR = "ASAMAL_DEV_DIR";

    String resolveTemplate(String controller, String view, String extension) throws ResourceNotFoundException;

    String resolvePartial(String controller, String partial, String extension) throws ResourceNotFoundException;

    InputStream resolveFile(String path) throws ResourceNotFoundException;

    interface Factory {
        ResourceResolver create(HttpServletRequest request);
    }
}
