package pl.softwaremill.asamal.exception;

import pl.softwaremill.asamal.extension.exception.AsamalException;

import javax.ws.rs.core.Response;

/**
 * User: szimano
 */
public class HttpErrorException extends AsamalException {

    private javax.ws.rs.core.Response.Status status;

    public HttpErrorException(javax.ws.rs.core.Response.Status status) {
        super();

        this.status = status;
    }

    public HttpErrorException(javax.ws.rs.core.Response.Status status, Throwable e) {
        super(e);

        this.status = status;
    }

    public HttpErrorException(Response.Status internalServerError, String msg) {
        super(msg);

        this.status = status;
    }

    public javax.ws.rs.core.Response.Status getStatus() {
        return status;
    }
}
