package pl.softwaremill.asamal.request.http;

import pl.softwaremill.asamal.AsamalParameters;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.DownloadDescription;
import pl.softwaremill.asamal.controller.FilterStopException;
import pl.softwaremill.asamal.controller.cdi.ControllerResolver;
import pl.softwaremill.asamal.controller.cdi.RequestType;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.producers.AsamalProducers;
import pl.softwaremill.asamal.request.AsamalViewHandler;
import pl.softwaremill.common.cdi.security.SecurityConditionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;

/**
 * User: szimano
 */
public class AbstractHttpHandler {

    protected AsamalProducers asamalProducers;

    protected AsamalViewHandler viewHandler;

    public AbstractHttpHandler(AsamalProducers asamalProducers, AsamalViewHandler viewHandler) {
        this.asamalProducers = asamalProducers;
        this.viewHandler = viewHandler;
    }

    public AbstractHttpHandler() {
    }

    protected AsamalContext createContext(HttpServletRequest req, HttpServletResponse resp, String extraPath) {
        AsamalContext context = new AsamalContext(req, resp, extraPath);
        asamalProducers.setAsamalContext(context);

        return context;
    }

    protected void createParamateres(HttpServletRequest req, MultivaluedMap<String, Object> formValues) {
        asamalProducers.setAsamalParameters(new AsamalParameters(req, formValues));
    }

    protected Response buildResponse(Object entity, String contentType) {
        String fileName = null;
        Object output = entity;

        if (entity instanceof DownloadDescription) {
            fileName = ((DownloadDescription) output).getFileName();
            output = ((DownloadDescription) output).getInputStream();
        }

        Response.ResponseBuilder responseBuilder = Response.status(Response.Status.OK)
                .entity(output)
                .header(HttpHeaders.CONTENT_TYPE, contentType);

        if (fileName != null) {
            responseBuilder = responseBuilder.header("Content-Disposition", "attachment; filename="+fileName);
        }

        return responseBuilder.build();
    }

    protected Response executeView(String controller, String view,
                                   HttpServletRequest req, HttpServletResponse resp, String extraPath,
                                   MultivaluedMap<String, Object> formValues, RequestType requrestType,
                                   byte[] content)
            throws HttpErrorException {
        // create the context
        AsamalContext context = createContext(req, resp, extraPath);
        createParamateres(req, formValues);

        ControllerBean controllerBean = null;

        try {
            ControllerResolver controllerResolver = ControllerResolver.resolveController(controller, content);

            Object viewResult = controllerResolver.executeView(requrestType, view);
            controllerBean = controllerResolver.getController();

            // if not redirecting and the right content type and void controller method - show the view
            if (!context.isWillRedirect()) {

                if (context.isWillInclude()) {
                    // change the view
                    view = context.getIncludeView();

                    // and execute it's controller
                    viewResult = controllerResolver.executeView(requrestType, view);
                }

                // now show the view if we are supposed to see html
                String contentType = controllerResolver.contentType(view, requrestType);

                if (ControllerResolver.HTML_CONTENT_TYPE.equals(contentType) && controllerResolver.isVoid(view, requrestType)) {
                    viewResult = viewHandler.showView(req, controllerBean, controller, view);
                }

                return buildResponse(viewResult, contentType);
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
