package pl.softwaremill.cdiweb.jaxrs;


import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import pl.softwaremill.cdiweb.controller.CDIWebContext;
import pl.softwaremill.cdiweb.controller.cdi.ControllerResolver;
import pl.softwaremill.cdiweb.controller.cdi.RequestType;
import pl.softwaremill.cdiweb.controller.ContextConstants;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Web;
import pl.softwaremill.cdiweb.controller.annotation.WebImpl;
import pl.softwaremill.cdiweb.exception.HttpErrorException;
import pl.softwaremill.cdiweb.resource.ResourceResolver;
import pl.softwaremill.cdiweb.servlet.CDIWebListener;
import pl.softwaremill.cdiweb.velocity.LayoutDirective;
import pl.softwaremill.cdiweb.velocity.TagHelper;
import pl.softwaremill.common.util.dependency.D;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

/**
 * User: szimano
 */
@Path("/")
public class JAXPostHandler {

    @GET
    @Path("/static/{path:.*}")
    public Object handleStaticGet(@Context HttpServletRequest req, @PathParam("path") String path) {
        return new ResourceResolver(req).resolveFile("/static/"+path);
    }

    @POST
    @Path("/post/{controller}/{view}{sep:/?}{path:.*}")
    public String handlePost(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                           @PathParam("controller") String controller, @PathParam("view") String view,
                           @PathParam("path") String extraPath,
                           MultivaluedMap<String, String> formValues) {

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            CDIWebContext context = new CDIWebContext(req, resp,
                    extraPath, formValues);

            controllerResolver.executeView(RequestType.POST, view, context);

            if (context.isWillInclude()) {
                return showView(req, controllerResolver.getController(), controller, context.getIncludeView());
            }

            return null;
        } catch (Exception e) {
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
        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            return controllerResolver.executeView(RequestType.JSON, view, new CDIWebContext(req, resp,
                    extraPath, null));
        } catch (Exception e) {
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

        ControllerBean controllerBean = null;

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            // create the context
            CDIWebContext context = new CDIWebContext(req, resp, extraPath, null);

            controllerResolver.executeView(RequestType.GET, view, context);
            controllerBean = controllerResolver.getController();

            // if not redirecting, show the view
            if (!context.isWillRedirect()) {

                if (context.isWillInclude()) {
                    // change the view
                    // this will just render a different view, it won't execute that view's method again
                    view = context.getIncludeView();
                }
                return showView(req, controllerBean, controller, view);
            }

            // will redirect
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

            BeanManager beanManager = (BeanManager) req.getServletContext().getAttribute(CDIWebListener.BEAN_MANAGER);

            Set<Bean<?>> beans = beanManager.getBeans(Object.class, new WebImpl());

            System.out.println("Listing beans");
            for (Bean<?> bean : beans) {
                System.out.println("bean: " + bean);
                for (Annotation annotation : bean.getQualifiers()) {
                    System.out.println("annotation = " + annotation.annotationType());
                    if (annotation.annotationType().equals(Web.class)) {
                        System.out.println("Adding annotation " + annotation);
                        context.put(((Web) annotation).value(), D.inject(bean.getBeanClass(),
                                bean.getQualifiers().toArray(new Annotation[bean.getQualifiers().size()])));

                        break;
                    }
                }
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
}
