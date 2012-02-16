package pl.softwaremill.asamal.controller.exception;

/**
 * Thrown when there is no desired parameter
 *
 * User: szimano
 */
public class NoSuchParameterException extends AutobindingException{
    public NoSuchParameterException(String msg) {
        super(msg);
    }
}
