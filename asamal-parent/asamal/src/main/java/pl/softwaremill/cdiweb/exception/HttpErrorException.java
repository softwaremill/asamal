package pl.softwaremill.cdiweb.exception;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.xml.ws.Response;

/**
 * User: szimano
 */
public class HttpErrorException extends CDIWebException {

    private javax.ws.rs.core.Response.Status status;

    public HttpErrorException(javax.ws.rs.core.Response.Status status) {
        super();

        this.status = status;
    }

    public HttpErrorException(javax.ws.rs.core.Response.Status status, Throwable e) {
        super(e);

        this.status = status;
    }

    public javax.ws.rs.core.Response.Status getStatus() {
        return status;
    }
}
