package pl.softwaremill.asamal.helper;

import pl.softwaremill.asamal.extension.view.PresentationContext;
import pl.softwaremill.asamal.viewhash.ViewHashGenerator;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;

/**
 * Tag Helper to use within presentation layer
 *
 * User: szimano
 */
public class AsamalHelper {

    private final String contextPath;
    private final PresentationContext presentationContext;

    @Inject
    public AsamalHelper(HttpServletRequest request, PresentationContext presentationContext) {
        this.contextPath = request.getContextPath();
        this.presentationContext = presentationContext;
    }

    public String link(String controller, String view) {
        StringWriter sw = new StringWriter();

        return sw.append(contextPath).append("/").append(controller).
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
                .append(reRenderList).append(", '")
                // viewHash
                .append((String) presentationContext.get(ViewHashGenerator.VIEWHASH)).append("')")
                .toString();
    }

    public String download(String controller, String view) {
        StringWriter sw = new StringWriter();

        return sw.append(contextPath).append("/download/").append(controller).
                append("/").append(view).toString();
    }
    
    public String pdf(String controller, String view) {
        StringWriter sw = new StringWriter();

        return sw.append(contextPath).append("/pdf/").append(controller).
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
