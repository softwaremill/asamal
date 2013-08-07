package pl.softwaremill.asamal.request.http;

import com.lowagie.text.pdf.BaseFont;
import org.xhtmlrenderer.pdf.ITextRenderer;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.cdi.RequestType;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.extension.exception.ResourceNotFoundException;
import pl.softwaremill.asamal.extension.view.ResourceResolver;
import pl.softwaremill.asamal.producers.AsamalProducers;
import pl.softwaremill.asamal.request.AsamalViewHandler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * POST handlers implemented with JAXRS
 *
 * User: szimano
 */
@Path("/")
public class GetHandler extends AbstractHttpHandler {

    private ResourceResolver.Factory resourceResolverFactory;

    @Inject
    public GetHandler(AsamalProducers asamalProducers, AsamalViewHandler viewHandler,
                      ResourceResolver.Factory resourceResolverFactory) {
        super(asamalProducers, viewHandler);
        this.resourceResolverFactory = resourceResolverFactory;
    }

    public GetHandler() {
    }

    @GET
    @Path("/")
    public String handleRootGet(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
        new AsamalContext(req, resp, null).redirect("home", "index", null);

        return null;
    }

    @GET
    @Path("/static/{path:.*}")
    public Object handleStaticGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                  @PathParam("path") String path) throws HttpErrorException {
        try {
            return resourceResolverFactory.create(req).resolveFile("/static/" + path);
        } catch (ResourceNotFoundException e) {
            throw new HttpErrorException(Response.Status.NOT_FOUND, e);
        }
    }

    @GET
    @Path("/asamal/{path:.*}")
    public Object handleStaticAsamalWebGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                        @PathParam("path") String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("/resources/" + path);
    }

    @GET
    @Path("/pdf/{controller}/{view}{sep:/?}{path:.*}")
    @Produces("application/pdf")
    public Object handlePDFGet(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                               @PathParam("controller") String controller,
                               @PathParam("view") String view, @PathParam("path") String extraPath,
                               byte[] content)
            throws HttpErrorException {
        Response page = handleGet(req, resp, controller, view, extraPath, content);

        if (page == null)
            return null;

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(
                    new ByteArrayInputStream(page.getEntity().toString()
                            .replaceAll("\\&ouml;", "ö").replaceAll("\\&Ouml;", "Ö")
                            .replaceAll("\\&oacute;", "ó").replaceAll("\\&Oacute;", "Ó").replaceAll("\\&nbsp;", " ")
                            .replaceAll("\\&uuml;", "ü").replaceAll("\\&Uuml;", "Ü")
                            .getBytes("UTF-8")));

            ITextRenderer renderer = new ITextRenderer();
            renderer.getFontResolver().addFont("fonts/Pfennig.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/PfennigBold.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/PfennigBoldItalic.ttf",
            BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/PfennigItalic.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/Cousine-Bold-Latin.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/Cousine-BoldItalic-Latin.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/Cousine-Italic-Latin.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/Cousine-Regular-Latin.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/Alegreya-Black.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/Alegreya-BlackItalic.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/Alegreya-Bold.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/Alegreya-BoldItalic.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/Alegreya-Italic.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.getFontResolver().addFont("fonts/Alegreya-Regular.ttf",
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
                              @PathParam("view") String view, @PathParam("path") String extraPath,
                              byte[] content)
            throws HttpErrorException {
        return executeView(controller, view, req, resp, extraPath, null, RequestType.GET, content);
    }

}
