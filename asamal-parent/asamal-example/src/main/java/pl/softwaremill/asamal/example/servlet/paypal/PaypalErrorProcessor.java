package pl.softwaremill.asamal.example.servlet.paypal;

import pl.softwaremill.asamal.example.service.email.EmailService;
import pl.softwaremill.common.paypal.process.PayPalErrorHandler;

/**
 * Handles the error
 */
public class PaypalErrorProcessor extends PayPalErrorHandler {

    private EmailService emailService;

    public PaypalErrorProcessor(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void processErrorMessage(ErrorMessage errorMessage) {
        // send the email and output to the console

        System.err.println(errorMessage.toString());

        emailService.sendPaypalError(errorMessage.toString());
    }
}
