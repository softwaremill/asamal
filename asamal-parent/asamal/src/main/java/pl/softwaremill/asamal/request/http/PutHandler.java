package pl.softwaremill.asamal.request.http;

import pl.softwaremill.asamal.controller.cdi.RequestType;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.producers.AsamalProducers;
import pl.softwaremill.asamal.request.AsamalViewHandler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/")
public class PutHandler extends AbstractHttpHandler {

    @Inject
    public PutHandler(AsamalProducers asamalProducers, AsamalViewHandler viewHandler) {
        super(asamalProducers, viewHandler);
    }

    public PutHandler() {
    }

    @PUT
    @Path("/{controller}/{view}{sep:/?}{path:.*}")
    public Response handlePut(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                              @PathParam("controller") String controller,
                              @PathParam("view") String view, @PathParam("path") String extraPath,
                              byte[] content)
            throws HttpErrorException {
        return executeView(controller, view, req, resp, extraPath, null, RequestType.PUT, content);
    }

}
