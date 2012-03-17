package pl.softwaremill.asamal.jaxrs;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolManager;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.softwaremill.asamal.AsamalParameters;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ContextConstants;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.FilterStopException;
import pl.softwaremill.asamal.controller.cdi.BootstrapCheckerExtension;
import pl.softwaremill.asamal.controller.cdi.ControllerResolver;
import pl.softwaremill.asamal.controller.cdi.RequestType;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.exception.IllegalIncludeRedirectException;
import pl.softwaremill.asamal.i18n.Messages;
import pl.softwaremill.asamal.producers.AsamalProducers;
import pl.softwaremill.asamal.resource.ResourceResolver;
import pl.softwaremill.asamal.servlet.AsamalListener;
import pl.softwaremill.asamal.velocity.LayoutDirective;
import pl.softwaremill.asamal.velocity.TagHelper;
import pl.softwaremill.asamal.viewhash.ViewDescriptor;
import pl.softwaremill.common.cdi.security.SecurityConditionException;
import pl.softwaremill.common.util.dependency.D;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * User: szimano
 */
@Path("/")
public class JAXPostHandler {

    private ResourceResolver.Factory resourceResolverFactory;

    public static final String VIEWHASH = "asamalViewHash";
    public static final String VIEWHASH_MAP = VIEWHASH + "Map";

    private BootstrapCheckerExtension bootstrapCheckerExtension;
    private AsamalProducers asamalProducers;

    @Inject
    public JAXPostHandler(ResourceResolver.Factory resourceResolverFactory,
                          BootstrapCheckerExtension bootstrapCheckerExtension,
                          AsamalProducers asamalProducers) {
        this.resourceResolverFactory = resourceResolverFactory;
        this.bootstrapCheckerExtension = bootstrapCheckerExtension;
        this.asamalProducers = asamalProducers;
    }

    public JAXPostHandler() {
    }

    @GET
    @Path("/")
    public String handleRootGet(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        new AsamalContext(request, response, null).redirect("home", "index");

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
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("/resources/" + path);
    }

    @POST
    @Path("/post-formdata/{controller}/{view}{sep:/?}{path:.*}")
    @Consumes("multipart/form-data")
    public String handlePostFormData(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                     @PathParam("controller") String controller, @PathParam("view") String view,
                                     @PathParam("path") String extraPath,
                                     MultipartFormDataInput multiInput) throws HttpErrorException {
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

        try {
            return handleCommonPost(req, resp, controller, view, extraPath, formValues, false);
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
                             MultivaluedMap<String, String> formValues) throws HttpErrorException {

        try {
            return handleCommonPost(req, resp, controller, view, extraPath, rewriteStringToObject(formValues), false);
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
        createContext(req, resp, extraPath);

        createParamateres(req, null);

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
                    true);
        } catch (IllegalIncludeRedirectException e) {
            throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }

        List<String> reRenderList = formValues.get("reRenderList");

        Document document = Jsoup.parse(output);

        Map<String, String> pageMap = new HashMap<String, String>();
        for (String id : reRenderList) {
            Elements elements = document.select("#" + id);

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
                                    MultivaluedMap<String, Object> formValues, boolean reRenderingPost)
            throws IllegalIncludeRedirectException, HttpErrorException {

        // create the context
        AsamalContext context = createContext(req, resp, extraPath);
        createParamateres(req, formValues);

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            if (reRenderingPost || !controllerResolver.skipViewHash(view)) {
                // for all post queries, they have to include the view hash
                List<Object> viewHashes = formValues.get(VIEWHASH);
                if (viewHashes != null) {
                    // now check all fo them (if there's > 1, then probably user is sending a field with the same name
                    // - he might be trying to cheat the system, so we're gonna show him it's not good ;-)

                    Map<String, ViewDescriptor> viewHashMap = getViewHashMap(req);
                    for (Object viewHash : viewHashes) {
                        if (!viewHashMap.containsKey(viewHash)) {
                            throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, "View hash " + viewHash +
                                    " was not found in the ViewHashMap.");
                        }
                    }
                } else {
                    throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR,
                            "There is no viewHash send for this post query");
                }
            }

            controllerResolver.executeView(RequestType.POST, view);

            if (reRenderingPost && (context.isWillInclude() || context.isWillRedirect())) {
                // do not allow this
                throw new IllegalIncludeRedirectException("Redirect and include is not allowed");
            }

            if (context.isWillInclude()) {
                // remove the messages from the flash, otherwise they will show up twice
                for (AsamalContext.MessageSeverity ms : AsamalContext.MessageSeverity.values()) {
                    req.removeAttribute(AsamalContext.FLASH_PREFIX + ms.name());
                }

                return showView(req, controllerResolver.getController(), controller, context.getIncludeView());
            } else if (reRenderingPost) {
                // include the previous view
                ViewDescriptor viewDescriptor = getViewHashMap(req).get(formValues.getFirst(VIEWHASH));

                return showView(req, controllerResolver.getController(), viewDescriptor.getController(),
                        viewDescriptor.getView());
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
        AsamalContext context = createContext(req, resp, extraPath);
        createParamateres(req, null);

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
            // create new view hash

            String viewHash = createNewViewHash(req, controller, view);

            ResourceResolver resourceResolver = resourceResolverFactory.create(req);

            ToolManager toolManager = new ToolManager(true, true);
            ToolContext context = toolManager.createContext();

            // set the viewHash
            context.put(VIEWHASH, viewHash);

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
            if (element.attr("method").toLowerCase().equals("post")) {
                Element formInputWithHash = document.createElement("input");
                formInputWithHash.attr("type", "hidden");
                formInputWithHash.attr("name", VIEWHASH);
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

    private String createNewViewHash(HttpServletRequest request, String controller, String view) {
        ViewDescriptor viewDescriptor = new ViewDescriptor(controller, view);

        String viewHash = UUID.randomUUID().toString();

        Map<String, ViewDescriptor> viewHashMap = getViewHashMap(request);

        viewHashMap.put(viewHash, viewDescriptor);

        return viewHash;
    }

    private Map<String, ViewDescriptor> getViewHashMap(HttpServletRequest request) {
        Map<String, ViewDescriptor> viewHashMap = (Map<String, ViewDescriptor>) request.getSession()
                .getAttribute(VIEWHASH_MAP);

        if (viewHashMap == null) {
            // the hash map is not yet defined
            // the map is synchronized, because one user might actually perform simultaneous requests
            request.getSession().setAttribute(VIEWHASH_MAP, viewHashMap =
                    Collections.synchronizedMap(new HashMap<String, ViewDescriptor>()));
        }

        return viewHashMap;
    }

    private AsamalContext createContext(HttpServletRequest req, HttpServletResponse resp, String extraPath) {
        AsamalContext context = new AsamalContext(req, resp, extraPath);
        asamalProducers.setAsamalContext(context);

        return context;
    }

    private void createParamateres(HttpServletRequest req, MultivaluedMap<String, Object> formValues) {
        asamalProducers.setAsamalParameters(new AsamalParameters(req, formValues));
    }
}
