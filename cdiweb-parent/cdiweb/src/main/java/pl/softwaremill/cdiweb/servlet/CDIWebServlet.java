package pl.softwaremill.cdiweb.servlet;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.ControllerImpl;
import pl.softwaremill.common.util.dependency.D;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * This is the starting point for everything
 *
 * User: szimano
 */
@WebServlet(urlPatterns = "/*")
public class CDIWebServlet extends HttpServlet{

    @Override
    public void init() throws ServletException {
        super.init();

        Velocity.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // make the magic

        // by default go to home/index
        if (req.getRequestURI().equals(req.getContextPath())) {
            resp.sendRedirect(req.getContextPath() + "/home/index");
            return;
        }

        String[] path = req.getRequestURI().substring(req.getContextPath().length() + 1).split("/");

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

                for (Map.Entry<String, Object> param : controller.getParams().entrySet()) {
                    context.put(param.getKey(), param.getValue());
                }

                controller.clearParams();

                /* lets render a template */

                StringWriter w = new StringWriter();

                InputStream templateStream = req.getServletContext().getResourceAsStream(
                        "/WEB-INF/" + path[0] + "/" + path[1] + ".vm");

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
                // no such method, show 404
                resp.sendError(404, "Not found");

                return;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
