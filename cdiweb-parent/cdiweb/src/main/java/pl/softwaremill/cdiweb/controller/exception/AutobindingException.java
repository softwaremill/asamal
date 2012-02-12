package pl.softwaremill.cdiweb.controller.exception;

/**
 * Base exception for autobinding problems
 *
 * User: szimano
 */
public class AutobindingException extends RuntimeException{

    public AutobindingException(String msg) {
        super(msg);
    }

    public AutobindingException(Exception e) {
        super(e);
    }
}
