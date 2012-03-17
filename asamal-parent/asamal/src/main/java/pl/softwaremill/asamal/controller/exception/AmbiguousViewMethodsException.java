package pl.softwaremill.asamal.controller.exception;

/**
 * Throw when asamal cannot guess which method should be invoked
 */
public class AmbiguousViewMethodsException extends Exception {

    public AmbiguousViewMethodsException(String msg) {
        super(msg);
    }
}
