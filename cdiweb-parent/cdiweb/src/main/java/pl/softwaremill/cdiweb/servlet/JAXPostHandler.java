package pl.softwaremill.cdiweb.servlet;


import pl.softwaremill.cdiweb.cdi.ControllerResolver;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;

/**
 * User: szimano
 */
@Path("/")
public class JAXPostHandler {

    @POST
    @Path("/{controller}/{view}")
    public void handlePost(@PathParam("controller") String controller, @PathParam("view") String view,
                           MultivaluedMap<String,String> formValues) {

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            controllerResolver.getController().doPostMagic(formValues.entrySet());

            controllerResolver.executeView(view);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
