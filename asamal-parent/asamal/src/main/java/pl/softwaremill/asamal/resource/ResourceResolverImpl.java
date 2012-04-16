package pl.softwaremill.asamal.resource;

import pl.softwaremill.asamal.extension.exception.ResourceNotFoundException;
import pl.softwaremill.asamal.extension.view.ResourceResolver;
import pl.softwaremill.common.cdi.autofactory.CreatedWith;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

/**
 * Basic implementation of resource resolver.
 *
 * If ASAMAL_DEV_DIR is set as system property, it will look for files on the drive.
 *
 * Otherwise it will read them from within the Web Archive.
 *
 * User: szimano
 */
@CreatedWith(ResourceResolver.Factory.class)
public class ResourceResolverImpl implements ResourceResolver {

    private HttpServletRequest req;

    public ResourceResolverImpl(HttpServletRequest req) {
        this.req = req;
    }

    public String resolveTemplate(String controller, String view, String extension) throws ResourceNotFoundException {
        InputStream is = resolveFile("/view/" + controller + "/" + view + "." + extension);

        return readInputStream(is);
    }
    
    public String resolvePartial(String controller, String partial, String extension) throws ResourceNotFoundException {
        StringBuffer sb = new StringBuffer();

        sb.append("/view");

        if (partial.startsWith("/")) {
            // do not use the controller, and change the last segment to use "_"
            String[] segments = partial.split("/");
            
            for (int i = 0; i < segments.length; i++) {
                sb.append("/");

                if (i == segments.length - 1) {
                    sb.append("_");
                }

                sb.append(segments[i]);
            }

            sb.append(".").append(extension);
        }
        else {
            sb.append("/").append(controller).append("/_").append(partial).append(".").append(extension);
        }

        return readInputStream(resolveFile(sb.toString()));
    }

    public InputStream resolveFile(String path) throws ResourceNotFoundException {
        InputStream is;

        if (System.getProperty(ASAMAL_DEV_DIR) != null) {
            // read from the disk

            String dir = System.getProperty(ASAMAL_DEV_DIR);

            try {
                is = new FileInputStream(dir + path);
            } catch (FileNotFoundException e) {
                throw new ResourceNotFoundException(e);
            }
        } else {
            is = req.getServletContext().getResourceAsStream(path);
        }

        if (is == null) {
            throw new ResourceNotFoundException("Could not find resource "+path);
        }

        return is;
    }
    
    private String readInputStream(InputStream inputStream) throws ResourceNotFoundException {
        StringWriter templateSW = new StringWriter();
        try {
            Reader r = new InputStreamReader(inputStream, "UTF-8");

            int c;
            while ((c = r.read()) > 0) {
                templateSW.append((char) c);
            }
        } catch (IOException e) {
            throw new ResourceNotFoundException(e);
        }

        return templateSW.toString();
    }
}
