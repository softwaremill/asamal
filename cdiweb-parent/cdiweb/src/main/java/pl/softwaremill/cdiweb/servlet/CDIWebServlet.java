package pl.softwaremill.cdiweb.servlet;

import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.ControllerImpl;
import pl.softwaremill.common.util.dependency.D;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * This is the starting point for everything
 *
 * User: szimano
 */
@WebServlet(urlPatterns = "/*")
public class CDIWebServlet extends HttpServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // make the magic
        
        String[] path = req.getContextPath().split("/");

        System.out.println("Path: " + Arrays.toString(path));

        if (path.length != 2) {
            resp.sendError(500, "The path has to contain 2 elements");
        }
        else {
            Object controller = D.inject(ControllerBean.class, new ControllerImpl(path[0]));

            // get the method
            try {
                // get the view method
                Method method = controller.getClass().getDeclaredMethod(path[1]);

                // invoke it
                method.invoke(controller);

                resp.setContentType("text/html");

                PrintWriter writer = resp.getWriter();

                writer.write("<html><body>This works!</body></html>");

                writer.close();
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
