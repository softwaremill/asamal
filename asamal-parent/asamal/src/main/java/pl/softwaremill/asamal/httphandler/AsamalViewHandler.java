package pl.softwaremill.asamal.httphandler;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ContextConstants;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.cdi.BootstrapCheckerExtension;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.i18n.Messages;
import pl.softwaremill.asamal.resource.ResourceResolver;
import pl.softwaremill.asamal.servlet.AsamalListener;
import pl.softwaremill.asamal.velocity.AsamalHelper;
import pl.softwaremill.asamal.velocity.LayoutDirective;
import pl.softwaremill.asamal.viewhash.ViewHashGenerator;
import pl.softwaremill.common.util.dependency.D;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Map;

/**
 * User: szimano
 */
public class AsamalViewHandler {

    private BootstrapCheckerExtension bootstrapCheckerExtension;
    private ViewHashGenerator viewHashGenerator;
    private ResourceResolver.Factory resourceResolverFactory;

    @Inject
    public AsamalViewHandler(BootstrapCheckerExtension bootstrapCheckerExtension, ViewHashGenerator viewHashGenerator,
                             ResourceResolver.Factory resourceResolverFactory) {
        this.bootstrapCheckerExtension = bootstrapCheckerExtension;
        this.viewHashGenerator = viewHashGenerator;
        this.resourceResolverFactory = resourceResolverFactory;
    }

    public AsamalViewHandler() {
    }

    public String showView(HttpServletRequest req, ControllerBean controllerBean, String controller, String view)
            throws HttpErrorException {
        try {
            // create new view hash

            String viewHash = viewHashGenerator.createNewViewHash(controller, view);

            ResourceResolver resourceResolver = resourceResolverFactory.create(req);

            ToolManager toolManager = new ToolManager(true, true);
            ToolContext context = toolManager.createContext();

            // set the viewHash
            context.put(ViewHashGenerator.VIEWHASH, viewHash);

            // set the resolver
            context.put(ContextConstants.RESOURCE_RESOLVER, resourceResolver);

            // set i18n messages
            context.put(ContextConstants.MESSAGES, new Messages());


            for (Class clazz : bootstrapCheckerExtension.getNamedBeans()) {
                String name = ((Named) clazz.getAnnotation(Named.class)).value();

                // get the qualifiers from that bean, so it gets injected no matter what
                BeanManager bm = (BeanManager) req.getServletContext().getAttribute(AsamalListener.BEAN_MANAGER);

                // iterate through all of them, and remember which ones are qualifiers
                ArrayList<Annotation> qualifiers = new ArrayList<Annotation>();
                for (Annotation annotation : clazz.getDeclaredAnnotations()) {
                    if (bm.isQualifier(annotation.annotationType())) {
                        qualifiers.add(annotation);
                    }
                }

                context.put(name, D.inject(clazz, qualifiers.toArray(new Annotation[qualifiers.size()])));
            }

            for (Map.Entry<String, Object> param : controllerBean.getParams().entrySet()) {
                context.put(param.getKey(), param.getValue());
            }

            // put some context
            AsamalHelper asamalHelper = new AsamalHelper(req.getContextPath(), context);

            context.put(ContextConstants.ASAMAL_HELPER, asamalHelper);
            context.put(ContextConstants.ASAMAL_HELPER_OLD, asamalHelper);
            context.put(ContextConstants.PAGE_TITLE, controllerBean.getPageTitle());
            context.put(ContextConstants.CONTROLLER, controllerBean);
            context.put(ContextConstants.VIEW, view);

            for (AsamalContext.MessageSeverity severity : AsamalContext.MessageSeverity.values()) {
                context.put(severity.name().toLowerCase(), req.getAttribute(severity.name()));
            }

            controllerBean.clearParams();

            /* lets render a template */

            StringWriter w = new StringWriter();

            String template = resourceResolver.resolveTemplate(controller, view);

            Velocity.evaluate(context, w, controller + "/" + view, template);

            String layout;
            while ((layout = (String) context.get(LayoutDirective.LAYOUT)) != null) {
                // clear the layout
                context.put(LayoutDirective.LAYOUT, null);

                w = new StringWriter();
                template = resourceResolver.resolveTemplate("layout", layout);
                Velocity.evaluate(context, w, controller + "/" + view, template);
            }

            String outputHtml = w.toString();

            //finally enhance the html by adding some asamal magic
            return enhanceOutputHtml(req, outputHtml, viewHash);
        } catch (Exception e) {
            e.printStackTrace();

            throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    private String enhanceOutputHtml(HttpServletRequest request, String html, String viewHash) {
        Document document = Jsoup.parse(html);

        // for every POST form, add viewHash in hidden element
        Elements elements = document.select("form");

        for (Element element : elements) {
            // on all forms, set the UTF-8
            element.attr("accept-charset", "UTF-8");

            if (element.attr("method").toLowerCase().equals("post")) {
                Element formInputWithHash = document.createElement("input");
                formInputWithHash.attr("type", "hidden");
                formInputWithHash.attr("name", ViewHashGenerator.VIEWHASH);
                formInputWithHash.val(viewHash);

                element.appendChild(formInputWithHash);
            }
        }

        // in the head add asamal resource links
        Element head = document.select("head").get(0);

        Element asamalJS = document.createElement("script");
        asamalJS.attr("type", "text/javascript");
        asamalJS.attr("src", request.getContextPath() + "/asamal/asamal.js");

        head.appendChild(asamalJS);

        return document.html();
    }
}
