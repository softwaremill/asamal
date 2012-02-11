package pl.softwaremill.cdiweb.resource;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

/**
 * User: szimano
 */
public class ResourceResolver {

    private HttpServletRequest req;

    public ResourceResolver(HttpServletRequest req) {
        this.req = req;
    }

    public static final String CDIWEB_DEV_DIR = "CDIWEB_DEV_DIR";

    public String resolveTemplate(String controller, String view) throws IOException {
        InputStream is = resolveFile("/view/" + controller + "/" + view + ".vm");

        return readInputStream(is);
    }
    
    public String resolvePartial(String controller, String partial) throws IOException {
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

            sb.append(".vm");
        }
        else {
            sb.append("/").append(controller).append("/_").append(partial).append(".vm");
        }

        return readInputStream(resolveFile(sb.toString()));
    }

    public InputStream resolveFile(String path) {
        InputStream is;

        if (System.getProperty(CDIWEB_DEV_DIR) != null) {
            // read from the disk

            String dir = System.getProperty(CDIWEB_DEV_DIR);

            try {
                is = new FileInputStream(dir + path);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            is = req.getServletContext().getResourceAsStream(path);
        }

        return is;
    }
    
    private String readInputStream(InputStream inputStream) throws IOException {
        StringWriter templateSW = new StringWriter();

        int c;
        while ((c = inputStream.read()) > 0) {
            templateSW.append((char) c);
        }

        return templateSW.toString();
    }
}
