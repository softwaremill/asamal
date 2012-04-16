package pl.softwaremill.asamal.exception;

import pl.softwaremill.asamal.extension.exception.AsamalException;

/**
 * User: szimano
 */
public class IllegalIncludeRedirectException extends AsamalException {
    public IllegalIncludeRedirectException(String msg) {
        super(msg);
    }
}
