package pl.softwaremill.asamal.controller.exception;

/**
 * Throw when a parameter is required, but cannot be found
 */
public class RequiredParameterNotFoundException extends Exception {

    public RequiredParameterNotFoundException(String msg) {
        super(msg);
    }
}
