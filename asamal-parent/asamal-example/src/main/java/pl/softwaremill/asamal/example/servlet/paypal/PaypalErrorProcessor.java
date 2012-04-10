package pl.softwaremill.asamal.example.servlet.paypal;

import pl.softwaremill.common.paypal.process.PayPalErrorHandler;

/**
 * Handles the error
 */
public class PaypalErrorProcessor extends PayPalErrorHandler {

    @Override
    public void processErrorMessage(ErrorMessage errorMessage) {
        // send the email

        System.err.println(errorMessage.toString());
    }
}
