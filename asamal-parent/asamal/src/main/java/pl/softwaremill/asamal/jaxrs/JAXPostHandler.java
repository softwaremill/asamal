package pl.softwaremill.asamal.jaxrs;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ContextConstants;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.FilterStopException;
import pl.softwaremill.asamal.controller.annotation.Web;
import pl.softwaremill.asamal.controller.cdi.BootstrapCheckerExtension;
import pl.softwaremill.asamal.controller.cdi.ControllerResolver;
import pl.softwaremill.asamal.controller.cdi.RequestType;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.exception.IllegalIncludeRedirectException;
import pl.softwaremill.asamal.resource.ResourceResolver;
import pl.softwaremill.asamal.servlet.AsamalListener;
import pl.softwaremill.asamal.velocity.LayoutDirective;
import pl.softwaremill.asamal.velocity.TagHelper;
import pl.softwaremill.common.cdi.security.SecurityConditionException;
import pl.softwaremill.common.util.dependency.D;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: szimano
 */
@Path("/")
public class JAXPostHandler {

    private ResourceResolver.Factory resourceResolverFactory;

    private final static ThreadLocal<AsamalContext> asamalContextHolder = new ThreadLocal<AsamalContext>();
    
    private static final String FROM_CONTROLLER = "asamalFromController";
    private static final String FROM_VIEW = "asamalFromView";

    @Inject
    public JAXPostHandler(ResourceResolver.Factory resourceResolverFactory) {
        this.resourceResolverFactory = resourceResolverFactory;
    }

    public JAXPostHandler() {}

    @GET
    @Path("/")
    public String handleRootGet(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        new AsamalContext(request, response, null, null).redirect("home", "index");

        return null;
    }

    @GET
    @Path("/static/{path:.*}")
    public Object handleStaticGet(@Context HttpServletRequest req, @PathParam("path") String path) {
        return resourceResolverFactory.create(req).resolveFile("/static/" + path);
    }

    @GET
    @Path("/asamal/{path:.*}")
    public Object handleStaticCCDWebGet(@Context HttpServletRequest req, @PathParam("path") String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("/resources/"+path);
    }

    @POST
    @Path("/post-formdata/{controller}/{view}{sep:/?}{path:.*}")
    @Consumes("multipart/form-data")
    public String handlePostFormData(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                     @PathParam("controller") String controller, @PathParam("view") String view,
                                     @PathParam("path") String extraPath,
                                     MultipartFormDataInput multiInput) {
        // create a multivalued map, to pass to the regulas post method

        MultivaluedMap<String, Object> formValues = new MultivaluedMapImpl<String, Object>();

        for (Map.Entry<String, List<InputPart>> entry : multiInput.getFormDataMap().entrySet()) {
            for (InputPart inputPart : entry.getValue()) {
                try {
                    if (inputPart.getMediaType().getType().equals("text")) {
                        formValues.add(entry.getKey(), inputPart.getBodyAsString());
                    } else {
                        formValues.add(entry.getKey(), inputPart.getBody(InputStream.class, null));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Problem reading the multipart form", e);
                }
            }
        }
        System.out.println("formValues = " + formValues.keySet());
        try {
            return handleCommonPost(req, resp, controller, view, extraPath, formValues, true);
        } catch (IllegalIncludeRedirectException e) {
            // this will never happen
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/post/{controller}/{view}{sep:/?}{path:.*}")
    public String handlePost(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                             @PathParam("controller") String controller, @PathParam("view") String view,
                             @PathParam("path") String extraPath,
                             MultivaluedMap<String, String> formValues) {

        try {
            return handleCommonPost(req, resp, controller, view, extraPath, rewriteStringToObject(formValues), true);
        } catch (IllegalIncludeRedirectException e) {
            // this will never happen
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/json/{controller}/{view}{sep:/?}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object handleJsonGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                @PathParam("controller") String controller,
                                @PathParam("view") String view, @PathParam("path") String extraPath)
            throws HttpErrorException {

        // create the context
        AsamalContext context = new AsamalContext(req, resp, extraPath, null);
        asamalContextHolder.set(context);

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            return controllerResolver.executeView(RequestType.JSON, view);
        } catch (FilterStopException e) {
            // stop execution
            return null;
        } catch (Exception e) {
            throw new HttpErrorException(Response.Status.NOT_FOUND, e);
        }
    }

    @POST
    @Path("/rerender/{controller}/{view}{sep:/?}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> handleRerenderPost(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                               @PathParam("controller") String controller,
                                               @PathParam("view") String view, @PathParam("path") String extraPath,
                                               MultivaluedMap<String, String> formValues)
            throws HttpErrorException {

        String output = null;
        try {
            output = handleCommonPost(req, resp, controller, view, extraPath, rewriteStringToObject(formValues),
                    false);
        } catch (IllegalIncludeRedirectException e) {
            throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }

        List<String> reRenderList = formValues.get("reRenderList");

        Document document = Jsoup.parse(output);

        Map<String, String> pageMap = new HashMap<String, String>();
        for (String id : reRenderList) {
            Elements elements = document.select("#"+id);

            pageMap.put(id, elements.html());
        }

        return pageMap;
    }

    private MultivaluedMap<String, Object> rewriteStringToObject(MultivaluedMap<String, String> values) {
        MultivaluedMap<String, Object> map = new MultivaluedMapImpl<String, Object>();

        if (values != null) {
            for (Map.Entry<String, List<String>> entry : values.entrySet()) {
                for (String s : entry.getValue()) {
                    map.add(entry.getKey(), s);
                }
            }
        }

        return map;
    }
    
    private String handleCommonPost(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                             @PathParam("controller") String controller,
                             @PathParam("view") String view, @PathParam("path") String extraPath,
                             MultivaluedMap<String, Object> formValues, boolean allowIncludeAndRedirect)
                             throws IllegalIncludeRedirectException {
        // create the context
        AsamalContext context = new AsamalContext(req, resp, extraPath, formValues);
        asamalContextHolder.set(context);

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            controllerResolver.executeView(RequestType.POST, view);

            if (!allowIncludeAndRedirect && (context.isWillInclude() || context.isWillRedirect())) {
                // do not allow this
                throw new IllegalIncludeRedirectException("Redirect and include is not allowed");
            }

            if (context.isWillInclude()) {
                return showView(req, controllerResolver.getController(), controller, context.getIncludeView());
            } else if (formValues.containsKey(FROM_CONTROLLER) && formValues.containsKey(FROM_VIEW)) {
                // include the previous view
                return showView(req, controllerResolver.getController(), (String)formValues.getFirst(FROM_CONTROLLER),
                        (String)formValues.getFirst(FROM_VIEW));
            }

            return null;
        } catch (FilterStopException e) {
            // stop execution
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/{controller}/{view}{sep:/?}{path:.*}")
    @Produces(MediaType.TEXT_HTML)
    public String handleGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                            @PathParam("controller") String controller,
                            @PathParam("view") String view, @PathParam("path") String extraPath)
            throws HttpErrorException {

        // create the context
        AsamalContext context = new AsamalContext(req, resp, extraPath, null);
        asamalContextHolder.set(context);

        ControllerBean controllerBean = null;

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            controllerResolver.executeView(RequestType.GET, view);
            controllerBean = controllerResolver.getController();

            // if not redirecting, show the view
            if (!context.isWillRedirect()) {

                if (context.isWillInclude()) {
                    // change the view
                    view = context.getIncludeView();

                    // and execute it's controller
                    controllerResolver.executeView(RequestType.GET, view);
                }
                return showView(req, controllerBean, controller, view);
            }

            // will redirect
            return null;
        } catch (FilterStopException e) {
            // stop execution
            return null;
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof SecurityConditionException) {
                throw new HttpErrorException(Response.Status.FORBIDDEN, e);
            } else {
                throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
            }
        } catch (Exception e) {
            throw new HttpErrorException(Response.Status.NOT_FOUND, e);
        }
    }

    private String showView(HttpServletRequest req, ControllerBean controllerBean, String controller, String view)
            throws HttpErrorException {
        try {

            ResourceResolver resourceResolver = resourceResolverFactory.create(req);

            VelocityContext context = new VelocityContext();

            // set the resolver
            context.put("resourceResolver", resourceResolver);

            for (Class clazz : BootstrapCheckerExtension.webScopedBeans) {
                String webName = ((Web) clazz.getAnnotation(Web.class)).value();

                // get the qualifiers from that bean, so it gets injected no matter what
                BeanManager bm = (BeanManager) req.getServletContext().getAttribute(AsamalListener.BEAN_MANAGER);

                // iterate through all of them, and remember which ones are qualifiers
                ArrayList<Annotation> qualifiers = new ArrayList<Annotation>();
                for (Annotation annotation : clazz.getDeclaredAnnotations()) {
                    if (bm.isQualifier(annotation.annotationType())) {
                        qualifiers.add(annotation);
                    }
                }

                context.put(webName, D.inject(clazz, qualifiers.toArray(new Annotation[qualifiers.size()])));
            }

            for (Map.Entry<String, Object> param : controllerBean.getParams().entrySet()) {
                context.put(param.getKey(), param.getValue());
            }

            // put some context
            context.put(ContextConstants.TAG, new TagHelper(req.getContextPath(), context));
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

            return w.toString();
        } catch (Exception e) {
            e.printStackTrace();

            throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @javax.enterprise.inject.Produces
    public AsamalContext produceAsamalContext() {
        return asamalContextHolder.get();
    }
}
