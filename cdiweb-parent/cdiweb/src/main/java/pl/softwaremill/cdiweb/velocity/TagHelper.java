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
    
    public String formActionFormData(String controller, String view) {
        StringWriter sw = new StringWriter();

        return sw.append(contextPath).append("/post-formdata/").append(controller).
                append("/").append(view).toString();
    }
    
    public String link(String controller, String view) {
        StringWriter sw = new StringWriter();

        return sw.append(contextPath).append("/").append(controller).
                append("/").append(view).toString();
    }

    public String jsonLink(String controller, String view) {
        StringWriter sw = new StringWriter();

        return sw.append(contextPath).append("/json/").append(controller).
                append("/").append(view).toString();
    }
    
    public String jsLink(String jsName) {
        return staticLink("js", jsName);
    }

    public String cssLink(String cssName) {
        return staticLink("css", cssName);
    }

    public String imgLink(String imgName) {
        return staticLink("img", imgName);
    }
    
    private String staticLink(String type, String name) {
        StringWriter sw = new StringWriter();

        return sw.append(contextPath).append("/static/").append(type).append("/").append(name).toString();
    }
}
