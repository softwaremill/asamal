package pl.softwaremill.asamal.httphandler;

import com.lowagie.text.pdf.BaseFont;
import org.xhtmlrenderer.pdf.ITextRenderer;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.FilterStopException;
import pl.softwaremill.asamal.controller.cdi.ControllerResolver;
import pl.softwaremill.asamal.controller.cdi.RequestType;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.producers.AsamalProducers;
import pl.softwaremill.asamal.resource.ResourceResolver;
import pl.softwaremill.common.cdi.security.SecurityConditionException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * POST handlers implemented with JAXRS
 *
 * User: szimano
 */
@Path("/")
public class GetHandler extends AbstractHttpHandler {

    private AsamalViewHandler viewHandler;
    private ResourceResolver.Factory resourceResolverFactory;

    @Inject
    public GetHandler(AsamalProducers asamalProducers, AsamalViewHandler viewHandler,
                      ResourceResolver.Factory resourceResolverFactory) {
        super(asamalProducers);
        this.viewHandler = viewHandler;
        this.resourceResolverFactory = resourceResolverFactory;
    }

    public GetHandler() {
    }

    @GET
    @Path("/json/{controller}/{view}{sep:/?}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object handleJsonGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                @PathParam("controller") String controller,
                                @PathParam("view") String view, @PathParam("path") String extraPath)
            throws HttpErrorException {

        setHttpObjects(req, resp);

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

    @GET
    @Path("/")
    public String handleRootGet(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
        setHttpObjects(req, resp);

        new AsamalContext(req, resp, null).redirect("home", "index");

        return null;
    }

    @GET
    @Path("/static/{path:.*}")
    public Object handleStaticGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                  @PathParam("path") String path) {
        setHttpObjects(req, resp);

        return resourceResolverFactory.create(req).resolveFile("/static/" + path);
    }

    @GET
    @Path("/asamal/{path:.*}")
    public Object handleStaticCCDWebGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                        @PathParam("path") String path) {
        setHttpObjects(req, resp);

        return Thread.currentThread().getContextClassLoader().getResourceAsStream("/resources/" + path);
    }

    @GET
    @Path("/pdf/{controller}/{view}{sep:/?}{path:.*}")
    @Produces("application/pdf")
    public Object handlePDFGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                               @PathParam("controller") String controller,
                               @PathParam("view") String view, @PathParam("path") String extraPath)
            throws HttpErrorException {
        setHttpObjects(req, resp);

        Response page = handleGet(req, resp, controller, view, extraPath);

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(
                    new ByteArrayInputStream(page.getEntity().toString().getBytes("UTF-8")));

            ITextRenderer renderer = new ITextRenderer();
            renderer.getFontResolver().addFont("/Library/Fonts/Microsoft/Lucida Sans Unicode.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.setDocument(doc, null);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            renderer.layout();
            renderer.createPDF(os);
            os.close();

            return new ByteArrayInputStream(os.toByteArray());
        } catch (Exception e) {
            throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }



    @GET
    @Path("/{controller}/{view}{sep:/?}{path:.*}")
    public Response handleGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                              @PathParam("controller") String controller,
                              @PathParam("view") String view, @PathParam("path") String extraPath)
            throws HttpErrorException {
        setHttpObjects(req, resp);

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
                String viewHTML = viewHandler.showView(req, controllerBean, controller, view);
                return Response.status(Response.Status.OK)
                        .entity(viewHTML)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML+"; charset=UTF-8" )
                        .build();
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

}
