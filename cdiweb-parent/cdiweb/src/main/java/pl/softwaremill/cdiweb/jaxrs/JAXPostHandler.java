package pl.softwaremill.cdiweb.jaxrs;


import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import pl.softwaremill.cdiweb.controller.CDIWebContext;
import pl.softwaremill.cdiweb.controller.ContextConstants;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.FilterStopException;
import pl.softwaremill.cdiweb.controller.annotation.Web;
import pl.softwaremill.cdiweb.controller.cdi.BootstrapCheckerExtension;
import pl.softwaremill.cdiweb.controller.cdi.ControllerResolver;
import pl.softwaremill.cdiweb.controller.cdi.RequestType;
import pl.softwaremill.cdiweb.exception.HttpErrorException;
import pl.softwaremill.cdiweb.resource.ResourceResolver;
import pl.softwaremill.cdiweb.servlet.CDIWebListener;
import pl.softwaremill.cdiweb.velocity.LayoutDirective;
import pl.softwaremill.cdiweb.velocity.TagHelper;
import pl.softwaremill.common.util.dependency.D;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Map;

/**
 * User: szimano
 */
@Path("/")
public class JAXPostHandler {

    private final static ThreadLocal<CDIWebContext> cdiWebContextHolder = new ThreadLocal<CDIWebContext>();

    @GET
    @Path("/")
    public String handleRootGet(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        new CDIWebContext(request, response, null, null).redirect("home", "index");

        return null;
    }

    @GET
    @Path("/static/{path:.*}")
    public Object handleStaticGet(@Context HttpServletRequest req, @PathParam("path") String path) {
        return new ResourceResolver(req).resolveFile("/static/" + path);
    }

    @POST
    @Path("/post/{controller}/{view}{sep:/?}{path:.*}")
    public String handlePost(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                             @PathParam("controller") String controller, @PathParam("view") String view,
                             @PathParam("path") String extraPath,
                             MultivaluedMap<String, String> formValues) {

        // create the context
        CDIWebContext context = new CDIWebContext(req, resp, extraPath, null);
        cdiWebContextHolder.set(context);

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            controllerResolver.executeView(RequestType.POST, view);

            if (context.isWillInclude()) {
                return showView(req, controllerResolver.getController(), controller, context.getIncludeView());
            }

            return null;
        } catch (FilterStopException e) {
            // stop execution
            return null;
        }catch (Exception e) {
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
        CDIWebContext context = new CDIWebContext(req, resp, extraPath, null);
        cdiWebContextHolder.set(context);

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            return controllerResolver.executeView(RequestType.JSON, view);
        } catch (FilterStopException e) {
            // stop execution
            return null;
        }catch (Exception e) {
            throw new HttpErrorException(Response.Status.NOT_FOUND, e);
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
        CDIWebContext context = new CDIWebContext(req, resp, extraPath, null);
        cdiWebContextHolder.set(context);

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
        } catch (Exception e) {
            throw new HttpErrorException(Response.Status.NOT_FOUND, e);
        }
    }

    private String showView(HttpServletRequest req, ControllerBean controllerBean, String controller, String view)
            throws HttpErrorException {
        try {

            ResourceResolver resourceResolver = new ResourceResolver(req);

            VelocityContext context = new VelocityContext();

            // set the resolver
            context.put("resourceResolver", resourceResolver);

            System.out.println("Listing beans");
            for (Class clazz : BootstrapCheckerExtension.webScopedBeans) {
                System.out.println("bean: " + clazz);
                String webName = ((Web) clazz.getAnnotation(Web.class)).value();

                // get the qualifiers from that bean, so it gets injected no matter what
                BeanManager bm = (BeanManager) req.getServletContext().getAttribute(CDIWebListener.BEAN_MANAGER);

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
            context.put("tag", new TagHelper(req.getContextPath()));
            context.put("pageTitle", controllerBean.getPageTitle());
            context.put(ContextConstants.CONTROLLER, controllerBean);
            context.put(ContextConstants.VIEW, view);

            for (CDIWebContext.MessageSeverity severity : CDIWebContext.MessageSeverity.values()) {
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

            System.out.println(" template : " + w);

            return w.toString();
        } catch (Exception e) {
            e.printStackTrace();

            throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @javax.enterprise.inject.Produces
    public CDIWebContext produceCDIWebContext() {
        return cdiWebContextHolder.get();
    }
}
