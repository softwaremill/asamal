package pl.softwaremill.asamal.httphandler;

import pl.softwaremill.asamal.controller.DownloadDescription;
import pl.softwaremill.asamal.controller.FilterStopException;
import pl.softwaremill.asamal.controller.cdi.ControllerResolver;
import pl.softwaremill.asamal.controller.cdi.RequestType;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.extension.view.ResourceResolver;
import pl.softwaremill.asamal.producers.AsamalProducers;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * POST handlers implemented with JAXRS
 *
 * User: szimano
 */
@Path("/")
public class DownloadHandler extends AbstractHttpHandler {

    private AsamalViewHandler viewHandler;
    private ResourceResolver.Factory resourceResolverFactory;

    @Inject
    public DownloadHandler(AsamalProducers asamalProducers, AsamalViewHandler viewHandler,
                           ResourceResolver.Factory resourceResolverFactory) {
        super(asamalProducers);
        this.viewHandler = viewHandler;
        this.resourceResolverFactory = resourceResolverFactory;
    }

    public DownloadHandler() {
    }

    @GET
    @Path("/download/{controller}/{view}{sep:/?}{path:.*}")
    public Object handleDownloadGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                @PathParam("controller") String controller,
                                @PathParam("view") String view, @PathParam("path") String extraPath)
            throws HttpErrorException {

        setHttpObjects(req, resp);

        // create the context
        createContext(req, resp, extraPath);

        createParamateres(req, null);

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            Object output = controllerResolver.executeView(RequestType.DOWNLOAD, view);
            String fileName = null;

            if (output instanceof DownloadDescription) {
                fileName = ((DownloadDescription) output).getFileName();
                output = ((DownloadDescription) output).getInputStream();
            }

            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK)
                    .entity(output)
                    .header(HttpHeaders.CONTENT_TYPE, controllerResolver.contentType(view));

            if (fileName != null) {
                responseBuilder = responseBuilder.header("Content-Disposition", "attachment; filename="+fileName);
            }

            return responseBuilder.build();
        } catch (FilterStopException e) {
            // stop execution
            return null;
        } catch (Exception e) {
            throw new HttpErrorException(Response.Status.NOT_FOUND, e);
        }
    }

}
