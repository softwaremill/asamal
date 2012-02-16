package pl.softwaremill.asamal.jaxrs;

import pl.softwaremill.asamal.exception.HttpErrorException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * User: szimano
 */
@Provider
public class AsamalExceptionMapper implements ExceptionMapper<HttpErrorException> {

    public Response toResponse(HttpErrorException exception) {
        exception.printStackTrace();

        return Response.serverError().status(exception.getStatus()).build();
    }
}
