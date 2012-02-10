package pl.softwaremill.cdiweb.velocity;

import java.io.StringWriter;

/**
 * Tag Helper to use within velocity
 *
 * User: szimano
 */
public class TagHelper {

    private final String contextPath;

    public TagHelper(String contextPath) {
        this.contextPath = contextPath;
    }
    
    public String formAction(String controller, String view) {
        StringWriter sw = new StringWriter();
        
        return sw.append(contextPath).append("/post/").append(controller).
                append("/").append(view).toString();
    }
    
    public String link(String controller, String view) {
        StringWriter sw = new StringWriter();

        return sw.append(contextPath).append("/").append(controller).
                append("/").append(view).toString();
    }
}
