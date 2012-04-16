package pl.softwaremill.asamal.extension.exception;

public class ResourceNotFoundException extends AsamalException{
    public ResourceNotFoundException(Exception e) {
        super(e);
    }

    public ResourceNotFoundException(String msg) {
        super(msg);
    }
}
