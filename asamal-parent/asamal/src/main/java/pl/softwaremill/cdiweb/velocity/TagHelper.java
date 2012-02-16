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

    public String reRender(String controller, String view, String elementList, String reRenderList) {
        StringWriter sw = new StringWriter();

        return sw.append("doAjaxPost('")
                // url
                .append(contextPath).append("/rerender/").append(controller).append("/").append(view).append("', ")
                // element list
                .append(elementList).append(", ")
                // rerendering list
                .append(reRenderList).append(")")
                .toString();
    }
    
    public String jsLink(String jsName) {
        return staticLink("js", jsName);
    }

    public String cdiWebLinks() {
        StringWriter sw = new StringWriter();

        return sw.append("<script type='text/javascript' src='")
                .append(contextPath).append("/cdiweb/cdiweb.js'></script>\n").toString();
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
