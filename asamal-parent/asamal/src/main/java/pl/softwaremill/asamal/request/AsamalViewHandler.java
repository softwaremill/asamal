package pl.softwaremill.asamal.request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.cdi.AsamalAnnotationScanner;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.exception.NoViewFoundException;
import pl.softwaremill.asamal.extension.view.*;
import pl.softwaremill.asamal.helper.AsamalHelper;
import pl.softwaremill.asamal.i18n.Messages;
import pl.softwaremill.asamal.servlet.AsamalListener;
import pl.softwaremill.asamal.viewhash.ViewHashGenerator;
import pl.softwaremill.common.util.dependency.D;

import javax.enterprise.inject.Instance;
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

    private AsamalAnnotationScanner asamalAnnotationScanner;
    private ViewHashGenerator viewHashGenerator;
    private ResourceResolver.Factory resourceResolverFactory;
    private PresentationExtensionResolver presentationExtensionResolver;
    private Instance<AsamalHelper> asamalHelperInstance;

    @Inject
    public AsamalViewHandler(AsamalAnnotationScanner asamalAnnotationScanner, ViewHashGenerator viewHashGenerator,
                             ResourceResolver.Factory resourceResolverFactory,
                             PresentationExtensionResolver presentationExtensionResolver,
                             Instance<AsamalHelper> asamalHelperInstance) {
        this.asamalAnnotationScanner = asamalAnnotationScanner;
        this.viewHashGenerator = viewHashGenerator;
        this.resourceResolverFactory = resourceResolverFactory;
        this.presentationExtensionResolver = presentationExtensionResolver;
        this.asamalHelperInstance = asamalHelperInstance;
    }

    public AsamalViewHandler() {
    }

    public String showView(HttpServletRequest req, ControllerBean controllerBean, String controller, String view)
            throws HttpErrorException {
        try {
            // create new view hash

            String viewHash = viewHashGenerator.createNewViewHash(controller, view);

            ResourceResolver resourceResolver = resourceResolverFactory.create(req);

            PresentationExtension presentationExtension = null;
            try {
                presentationExtension = presentationExtensionResolver.
                        resolvePresentationExtension(resourceResolver, controller, view);
            } catch (NoViewFoundException e) {
                // just return null - no view available
                return null;
            }

            PresentationContext context = presentationExtension.createNewPresentationContext();

            // set the viewHash
            context.put(ViewHashGenerator.VIEWHASH, viewHash);

            // set the resolver
            context.put(ContextConstants.RESOURCE_RESOLVER, resourceResolver);

            // set i18n messages
            context.put(ContextConstants.MESSAGES, new Messages());


            for (Class clazz : asamalAnnotationScanner.getNamedBeans()) {
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
            AsamalHelper asamalHelper = asamalHelperInstance.get();

            context.put(ContextConstants.ASAMAL_HELPER, asamalHelper);
            context.put(ContextConstants.ASAMAL_HELPER_OLD, asamalHelper);
            context.put(ContextConstants.PAGE_TITLE, controllerBean.getPageTitle());
            context.put(ContextConstants.CONTROLLER, controllerBean);
            context.put(ContextConstants.CONTROLLER_NAME, controllerBean.getName());
            context.put(ContextConstants.VIEW, view);

            for (AsamalContext.MessageSeverity severity : AsamalContext.MessageSeverity.values()) {
                context.put(severity.name().toLowerCase(), req.getAttribute(severity.name()));
            }

            controllerBean.clearParams();

            /* lets render a template */

            StringWriter w = new StringWriter();

            String template = resourceResolver.resolveTemplate(controller, view, presentationExtension.getExtension());

            String outputHtml = presentationExtension.evaluateTemplate(context, resourceResolver, template);

            //finally enhance the html by adding some asamal magic
            return enhanceOutputHtml(req, outputHtml, controller, view, viewHash);
        } catch (Exception e) {
            e.printStackTrace();

            throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    private String enhanceOutputHtml(HttpServletRequest request, String html, String controller, String view,
                                     String viewHash) {
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

        Element viewJSVariables = document.createElement("script");
        viewJSVariables.attr("type", "text/javascript");
        viewJSVariables.append("var asamalController = '").append(controller).append("';\n");
        viewJSVariables.append("var asamalView = '").append(view).append("';\n");
        viewJSVariables.append("var asamalViewHash = '").append(viewHash).append("';\n");
        viewJSVariables.append("var asamalContextPath = '").append(request.getContextPath()).append("';\n");

        head.appendChild(asamalJS);
        head.appendChild(viewJSVariables);

        return document.html();
    }
}
