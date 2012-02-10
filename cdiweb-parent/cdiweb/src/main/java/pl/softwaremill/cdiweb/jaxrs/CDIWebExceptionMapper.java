package pl.softwaremill.cdiweb.jaxrs;

import pl.softwaremill.cdiweb.exception.HttpErrorException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * User: szimano
 */
@Provider
public class CDIWebExceptionMapper implements ExceptionMapper<HttpErrorException> {

    public Response toResponse(HttpErrorException exception) {
        exception.printStackTrace();

        return Response.serverError().status(exception.getStatus()).build();
    }
}
