package pl.softwaremill.cdiweb.jaxrs;


import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import pl.softwaremill.cdiweb.cdi.ControllerResolver;
import pl.softwaremill.cdiweb.cdi.RequestType;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Web;
import pl.softwaremill.cdiweb.controller.annotation.WebImpl;
import pl.softwaremill.cdiweb.exception.HttpErrorException;
import pl.softwaremill.cdiweb.servlet.CDIWebListener;
import pl.softwaremill.cdiweb.velocity.TagHelper;
import pl.softwaremill.common.util.dependency.D;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
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

    public static final String CDIWEB_DEV_DIR = "CDIWEB_DEV_DIR";

    @GET
    @Path("/static/{path:.*}")
    public Object handleStaticGet(@Context HttpServletRequest req, @PathParam("path") String path) {
        return resolveFile(req, path);
    }
    
    @POST
    @Path("/post/{controller}/{view}")
    public void handlePost(@PathParam("controller") String controller, @PathParam("view") String view,
                           MultivaluedMap<String, String> formValues) {

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            controllerResolver.getController().doPostMagic(formValues.entrySet());

            controllerResolver.executeView(RequestType.POST, view);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/{controller}/{view}")
    @Produces(MediaType.TEXT_HTML)
    public String handleGet(@Context HttpServletRequest req, @PathParam("controller") String controller,
                            @PathParam("view") String view) throws HttpErrorException {
        ControllerBean controllerBean = null;

        try {
            controllerBean = ControllerResolver.resolveController(controller).executeView(RequestType.GET, view).
                    getController();
        } catch (Exception e) {
            throw new HttpErrorException(Response.Status.NOT_FOUND, e);
        }

        // get the method
        try {

            VelocityContext context = new VelocityContext();

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

            context.put("tag", new TagHelper(req.getContextPath()));
            context.put("pageTitle", controllerBean.getPageTitle());
            context.put("content", resolveTemplate(req, controller, view));

            controllerBean.clearParams();

            /* lets render a template */

            StringWriter w = new StringWriter();

            String template = resolveTemplate(req, "layout", controllerBean.getLayout());

            Velocity.evaluate(context, w, controller + "/" + view, template);

            System.out.println(" template : " + w);

            return w.toString();
        } catch (Exception e) {
            e.printStackTrace();

            throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }

    }


    protected String resolveTemplate(HttpServletRequest req, String controller, String view) throws IOException {
        InputStream is = resolveFile(req, "/WEB-INF/"+controller+"/"+view+".vm");

        StringWriter templateSW = new StringWriter();

        int c;
        while ((c = is.read()) > 0) {
            templateSW.append((char) c);
        }
        
        return templateSW.toString();
    }
    
    protected InputStream resolveFile(HttpServletRequest req, String path) {
        InputStream is;

        if (System.getProperty(CDIWEB_DEV_DIR) != null) {
            // read from the disk

            String dir = System.getProperty(CDIWEB_DEV_DIR);

            try {
                is = new FileInputStream(dir + path);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            is = req.getServletContext().getResourceAsStream(path);
        }

        return is;
    }
}
