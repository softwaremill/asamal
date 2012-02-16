package pl.softwaremill.asamal.common;

import org.apache.tools.ant.filters.StringInputStream;
import pl.softwaremill.asamal.resource.ResourceResolver;
import pl.softwaremill.common.cdi.autofactory.CreatedWith;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * FIXME for some reason Arquillian ignores JaxPostHandler dependency, so this is injected by hand
 *
 * User: szimano
 */
@CreatedWith(ResourceResolver.Factory.class)
public class TestResourceResolver implements ResourceResolver {

    public TestResourceResolver(HttpServletRequest request) {
    }

    public String resolveTemplate(String controller, String view) throws IOException {
        return controller + "/" + view;
    }

    public String resolvePartial(String controller, String partial) throws IOException {
        return controller + "/" + partial;
    }

    public InputStream resolveFile(String path) {
        return new StringInputStream(path);
    }
}
