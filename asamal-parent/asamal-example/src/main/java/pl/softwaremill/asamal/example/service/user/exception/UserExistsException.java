package pl.softwaremill.asamal.example.service.user.exception;


public class UserExistsException extends Exception {
    public UserExistsException(String s, Exception e) {
        super(s, e);
    }
}
