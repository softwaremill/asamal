package pl.softwaremill.cdiweb.servlet;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.ControllerImpl;
import pl.softwaremill.cdiweb.controller.annotation.Web;
import pl.softwaremill.cdiweb.controller.annotation.WebImpl;
import pl.softwaremill.cdiweb.velcoity.TagHelper;
import pl.softwaremill.common.util.dependency.D;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * This is the starting point for everything
 *
 * User: szimano
 */
@WebServlet(urlPatterns = "/*", loadOnStartup = 1)
public class CDIWebServlet extends HttpServlet{

    public static final String CDIWEB_DEV_DIR = "CDIWEB_DEV_DIR";

    public static TagHelper tagHelper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init();

        Velocity.init();

        // warn about dev mode
        if (System.getProperty(CDIWEB_DEV_DIR) != null) {
            System.out.println("****************************");
            System.out.println("****************************");
            System.out.println("*    Running in DEV mode   *");
            System.out.println("* DO NOT use on production *");
            System.out.println("****************************");
            System.out.println("****************************");
        }

        tagHelper = new TagHelper(config.getServletContext().getContextPath());

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // make the magic

        // by default go to home/index
        if (req.getRequestURI().equals(req.getContextPath())) {
            resp.sendRedirect(req.getContextPath() + "/home/index");
            return;
        }

        String[] path = req.getRequestURI().substring((req.getContextPath()).length() + 1).split("/");

        System.out.println("Path: " + Arrays.toString(path));

        if (path.length != 2) {
            resp.sendError(404, "Not found");
            return;
        }
        else {
            ControllerBean controller = null;
            try {
                controller = D.inject(ControllerBean.class, new ControllerImpl(path[0]));
            } catch (Exception e) {
                e.printStackTrace();
                // injection failed, show 404
                resp.sendError(404, "Not found");
                return;
            }

            // get the method
            try {
                // get the view method
                Method method = controller.getClass().getDeclaredMethod(path[1]);

                // invoke it
                method.invoke(controller);

                resp.setContentType("text/html");

                VelocityContext context = new VelocityContext();

                BeanManager beanManager = (BeanManager) req.getServletContext().getAttribute(CDIWebListener.BEAN_MANAGER);

                Set<Bean<?>> beans = beanManager.getBeans(Object.class, new WebImpl());

                System.out.println("Listing beans");
                for (Bean<?> bean : beans) {
                    System.out.println("bean: "+bean);
                    for (Annotation annotation : bean.getQualifiers()) {
                        System.out.println("annotation = " + annotation.annotationType());
                        if (annotation.annotationType().equals(Web.class)) {
                            System.out.println("Adding annotation "+annotation);
                            context.put(((Web)annotation).value(), D.inject(bean.getBeanClass(), 
                                    bean.getQualifiers().toArray(new Annotation[bean.getQualifiers().size()])));

                            break;
                        }
                    }
                }

                for (Map.Entry<String, Object> param : controller.getParams().entrySet()) {
                    context.put(param.getKey(), param.getValue());
                }

                context.put("tag", tagHelper);

                controller.clearParams();

                /* lets render a template */

                StringWriter w = new StringWriter();

                InputStream templateStream = resolveTemplate(req, path[0], path[1]);

                StringWriter templateSW = new StringWriter();
                
                int c;
                while ((c = templateStream.read()) > 0) {
                    templateSW.append((char)c);
                }

                Velocity.evaluate(context, w, path[0]+path[1], templateSW.toString());

                System.out.println(" template : " + w );

                PrintWriter writer = resp.getWriter();

                writer.write(w.toString());

                writer.close();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                
                // no such method, show 404
                resp.sendError(404, "Not found");

                return;
            } catch (Exception e) {
                e.printStackTrace();

                throw new ServletException(e);
            }
        }
    }

    protected InputStream resolveTemplate(HttpServletRequest req, String controller, String view) {
        if (System.getProperty(CDIWEB_DEV_DIR) != null) {
            // read from the disk
            
            String dir = System.getProperty(CDIWEB_DEV_DIR);

            try {
                return new FileInputStream(dir + "/WEB-INF/" + controller + "/" + view + ".vm");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            return req.getServletContext().getResourceAsStream("/WEB-INF/" + controller + "/" + view + ".vm");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
