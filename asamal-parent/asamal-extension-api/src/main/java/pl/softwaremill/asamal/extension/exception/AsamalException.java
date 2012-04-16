package pl.softwaremill.asamal.extension.exception;

/**
 * User: szimano
 */
public class AsamalException extends Exception {

    public AsamalException() {
        super();
    }

    public AsamalException(Throwable e) {
        super(e);
    }
    
    public AsamalException(String msg) {
        super(msg);
    }
}
