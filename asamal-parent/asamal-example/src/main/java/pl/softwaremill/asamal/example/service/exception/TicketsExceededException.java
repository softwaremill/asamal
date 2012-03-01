package pl.softwaremill.asamal.example.service.exception;

public class TicketsExceededException extends Exception {
    public TicketsExceededException(String message) {
        super(message);
    }
}
