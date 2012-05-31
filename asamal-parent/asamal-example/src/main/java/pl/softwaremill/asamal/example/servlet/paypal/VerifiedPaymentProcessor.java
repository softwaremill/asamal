package pl.softwaremill.asamal.example.servlet.paypal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotals;
import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotalsCounter;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.common.paypal.process.PayPalParameters;
import pl.softwaremill.common.paypal.process.processors.VerifiedPayPalProcessor;
import pl.softwaremill.common.paypal.process.status.PayPalStatus;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Processes verified payments
 */
public class VerifiedPaymentProcessor extends VerifiedPayPalProcessor {

    @Inject
    private TicketService ticketService;

    @Inject
    private ConfigurationBean configurationBean;

    @Inject
    private InvoiceTotalsCounter invoiceTotalsCounter;

    private static final Logger log = LoggerFactory.getLogger(VerifiedPaymentProcessor.class);

    @Override
    public void process(PayPalStatus status, PayPalParameters parameters) {
        try {
            Long invoiceId = Long.parseLong(parameters.getInvoice());

            Invoice invoice = ticketService.loadInvoice(invoiceId);

            InvoiceTotals invoiceTotals = invoiceTotalsCounter.countInvoice(invoice);

            // check the payment amount
            if (!invoiceTotals.getTotalGrossAmount().equals(new BigDecimal(parameters.getPaymentAmount()))) {
                appendError("Invoice amount is " + invoiceTotals.getTotalGrossAmount() + " but payment was for " +
                        parameters.getPaymentAmount());
                setErrorHappened();
                return;
            }

            // and the currency
            String currency = configurationBean.getProperty(Conf.INVOICE_CURRENCY);

            if (!parameters.getPaymentCurrency().equals(currency)) {
                appendError("Payments should be done in "+currency+" but got one in "+parameters.getPaymentCurrency());
                setErrorHappened();
                return;
            }

            // otherwise all allright
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setDatePaid(new Date());
            invoice.setInvoiceNumber(ticketService.getNextInvoiceNumber(invoice.getMethod()));

            ticketService.updateInvoice(invoice);

            log.info("Got successful payment for invoice id: "+invoiceId);
        } catch (NumberFormatException e) {
            appendError("Invoice id is not a proper Long: " + e.getMessage());
            setErrorHappened();
        }
    }
}
