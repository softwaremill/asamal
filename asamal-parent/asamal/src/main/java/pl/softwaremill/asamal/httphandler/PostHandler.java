package pl.softwaremill.asamal.httphandler;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.FilterStopException;
import pl.softwaremill.asamal.controller.cdi.ControllerResolver;
import pl.softwaremill.asamal.controller.cdi.RequestType;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.exception.IllegalIncludeRedirectException;
import pl.softwaremill.asamal.producers.AsamalProducers;
import pl.softwaremill.asamal.viewhash.ViewDescriptor;
import pl.softwaremill.asamal.viewhash.ViewHashGenerator;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * POST handlers implemented with JAXRS
 *
 * User: szimano
 */
@Path("/")
public class PostHandler extends AbstractHttpHandler{


    private ViewHashGenerator viewHashGenerator;
    private AsamalViewHandler viewHandler;

    @Inject
    public PostHandler(AsamalProducers asamalProducers,
                       ViewHashGenerator viewHashGenerator, AsamalViewHandler viewHandler) {
        super(asamalProducers);

        this.viewHashGenerator = viewHashGenerator;
        this.viewHandler = viewHandler;
    }

    public PostHandler() {
    }


    @POST
    @Path("/post-formdata/{controller}/{view}{sep:/?}{path:.*}")
    @Consumes("multipart/form-data")
    public String handlePostFormData(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                     @PathParam("controller") String controller, @PathParam("view") String view,
                                     @PathParam("path") String extraPath,
                                     MultipartFormDataInput multiInput) throws HttpErrorException {
        setHttpObjects(req, resp);

        // create a multivalued map, to pass to the regulas post method
        MultivaluedMap<String, Object> formValues = new MultivaluedMapImpl<String, Object>();

        for (Map.Entry<String, List<InputPart>> entry : multiInput.getFormDataMap().entrySet()) {
            for (InputPart inputPart : entry.getValue()) {
                try {
                    if (inputPart.getMediaType().getType().equals("text")) {
                        formValues.add(entry.getKey(), inputPart.getBodyAsString());
                    } else {
                        formValues.add(entry.getKey(), inputPart.getBody(InputStream.class, null));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Problem reading the multipart form", e);
                }
            }
        }

        try {
            return handleCommonPost(req, resp, controller, view, extraPath, formValues, false);
        } catch (IllegalIncludeRedirectException e) {
            // this will never happen
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/post/{controller}/{view}{sep:/?}{path:.*}")
    public String handlePost(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                             @PathParam("controller") String controller, @PathParam("view") String view,
                             @PathParam("path") String extraPath,
                             MultivaluedMap<String, String> formValues) throws HttpErrorException {

        setHttpObjects(req, resp);

        try {
            return handleCommonPost(req, resp, controller, view, extraPath, rewriteStringToObject(formValues), false);
        } catch (IllegalIncludeRedirectException e) {
            // this will never happen
            throw new RuntimeException(e);
        }
    }



    @POST
    @Path("/rerender/{controller}/{view}{sep:/?}{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> handleRerenderPost(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                                  @PathParam("controller") String controller,
                                                  @PathParam("view") String view, @PathParam("path") String extraPath,
                                                  MultivaluedMap<String, String> formValues)
            throws HttpErrorException {

        setHttpObjects(req, resp);

        String output = null;
        try {
            output = handleCommonPost(req, resp, controller, view, extraPath, rewriteStringToObject(formValues),
                    true);
        } catch (IllegalIncludeRedirectException e) {
            throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }

        List<String> reRenderList = formValues.get("reRenderList");

        Document document = Jsoup.parse(output);

        Map<String, String> pageMap = new HashMap<String, String>();
        for (String id : reRenderList) {
            Elements elements = document.select("#" + id);

            pageMap.put(id, elements.html());
        }

        return pageMap;
    }

    private MultivaluedMap<String, Object> rewriteStringToObject(MultivaluedMap<String, String> values) {
        MultivaluedMap<String, Object> map = new MultivaluedMapImpl<String, Object>();

        if (values != null) {
            for (Map.Entry<String, List<String>> entry : values.entrySet()) {
                for (String s : entry.getValue()) {
                    map.add(entry.getKey(), s);
                }
            }
        }

        return map;
    }

    private String handleCommonPost(@Context HttpServletRequest req, @Context HttpServletResponse resp,
                                    @PathParam("controller") String controller,
                                    @PathParam("view") String view, @PathParam("path") String extraPath,
                                    MultivaluedMap<String, Object> formValues, boolean reRenderingPost)
            throws IllegalIncludeRedirectException, HttpErrorException {

        // create the context
        AsamalContext context = createContext(req, resp, extraPath);
        createParamateres(req, formValues);

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller);

            if (reRenderingPost || !controllerResolver.skipViewHash(view)) {
                // for all post queries, they have to include the view hash
                List<Object> viewHashes = formValues.get(ViewHashGenerator.VIEWHASH);
                if (viewHashes != null) {
                    // now check all fo them (if there's > 1, then probably user is sending a field with the same name
                    // - he might be trying to cheat the system, so we're gonna show him it's not good ;-)

                    Map<String, ViewDescriptor> viewHashMap = viewHashGenerator.getViewHashMap();
                    for (Object viewHash : viewHashes) {
                        if (!viewHashMap.containsKey(viewHash)) {
                            throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR, "View hash " + viewHash +
                                    " was not found in the ViewHashMap.");
                        }
                    }
                } else {
                    throw new HttpErrorException(Response.Status.INTERNAL_SERVER_ERROR,
                            "There is no viewHash send for this post query");
                }
            }

            controllerResolver.executeView(RequestType.POST, view);

            if (reRenderingPost && (context.isWillInclude() || context.isWillRedirect())) {
                // do not allow this
                throw new IllegalIncludeRedirectException("Redirect and include is not allowed");
            }

            if (context.isWillInclude()) {
                // remove the messages from the flash, otherwise they will show up twice
                for (AsamalContext.MessageSeverity ms : AsamalContext.MessageSeverity.values()) {
                    req.removeAttribute(AsamalContext.FLASH_PREFIX + ms.name());
                }

                return viewHandler.showView(req,
                        controllerResolver.getController(), controller, context.getIncludeView());
            } else if (reRenderingPost) {
                // include the previous view
                ViewDescriptor viewDescriptor = viewHashGenerator.getViewHashMap().get(
                        formValues.getFirst(ViewHashGenerator.VIEWHASH));

                return viewHandler.showView(req, controllerResolver.getController(), viewDescriptor.getController(),
                        viewDescriptor.getView());
            }

            return null;
        } catch (FilterStopException e) {
            // stop execution
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
