package pl.softwaremill.asamal.common;

import org.junit.Ignore;
import pl.softwaremill.asamal.extension.exception.ResourceNotFoundException;
import pl.softwaremill.asamal.extension.view.ResourceResolver;
import pl.softwaremill.common.cdi.autofactory.CreatedWith;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * FIXME for some reason Arquillian ignores JaxPostHandler dependency, so this is injected by hand
 *
 * User: szimano
 */
@CreatedWith(ResourceResolver.Factory.class)
@Ignore
public class TestResourceResolver implements ResourceResolver {
    
    public static String returnHtml = null;

    public TestResourceResolver(HttpServletRequest request) {
    }

    public String resolveTemplate(String controller, String view, String extension)
            throws ResourceNotFoundException {
        if (returnHtml != null) {
            return returnHtml;
        }
        return controller + "/" + view;
    }

    public String resolvePartial(String controller, String partial, String extension)
            throws ResourceNotFoundException {
        return controller + "/" + partial;
    }

    public InputStream resolveFile(String path) {
        return new ByteArrayInputStream(path.getBytes());
    }
}
