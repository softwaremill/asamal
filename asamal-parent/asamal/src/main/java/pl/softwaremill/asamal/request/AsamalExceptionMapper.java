package pl.softwaremill.asamal.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.softwaremill.asamal.exception.HttpErrorException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * User: szimano
 */
@Provider
public class AsamalExceptionMapper implements ExceptionMapper<HttpErrorException> {

    private static final Logger log = LoggerFactory.getLogger(AsamalExceptionMapper.class);

    public Response toResponse(HttpErrorException exception) {
        log.debug("Got HTTP exception", exception);

        return Response.serverError().status(exception.getStatus()).build();
    }
}
