package pl.softwaremill.asamal.example.service.email;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotals;
import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotalsCounter;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.PaymentMethod;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.common.cdi.transaction.Transactional;
import pl.softwaremill.common.sqs.email.SendEmailTask;
import pl.softwaremill.common.sqs.util.EmailDescription;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;

public class EmailService {

    @Inject
    private ConfigurationBean configurationBean;

    @Inject
    private LoginBean loginBean;

    @Inject
    private InvoiceTotalsCounter invoiceTotalsCounter;

    @PersistenceContext
    private EntityManager entityManager;

    public void sendThankYouEmail(Invoice invoice) {
        VelocityContext context = new VelocityContext();

        InvoiceTotals invoiceTotals = invoiceTotalsCounter.countInvoice(invoice);

        context.put("name", invoice.getName());
        context.put("tickets", invoice.getTickets());
        context.put("paymentMethod", invoice.getMethod());

        context.put("amount",
                invoiceTotals.getTotalGrossAmount().setScale(2, BigDecimal.ROUND_HALF_DOWN).toString() + " "
                + configurationBean.getProperty(Conf.INVOICE_CURRENCY));
        if (invoice.getMethod() == PaymentMethod.TRANSFER) {
            context.put("invoiceId", "PROF/"+
                    configurationBean.getProperty(Conf.INVOICE_ID)+"TRANSFER/"+invoice.getId());
        }

        String emailTemplate = configurationBean.getProperty(Conf.TICKETS_THANKYOU_EMAIL);

        StringWriter sw = new StringWriter();
        Velocity.evaluate(context, sw, "emailThankYou", emailTemplate);

        EmailSendingBean.scheduleTask(new SendEmailTask(new EmailDescription(loginBean.getUser().getUsername(),
                sw.toString(), configurationBean.getProperty(Conf.TICKETS_THANKYOU_EMAIL_SUBJECT), null, null,
                configurationBean.getProperty(Conf.NOTIFY_EMAIL))));
    }

    public void sendInvoiceEmail(Invoice invoice) {
        VelocityContext context = new VelocityContext();

        context.put("name", invoice.getName());

        context.put("invoice_link", getInvoiceLink(invoice));

        String emailTemplate = configurationBean.getProperty(Conf.INVOICE_EMAIL);

        StringWriter sw = new StringWriter();
        Velocity.evaluate(context, sw, "emailInvoice", emailTemplate);

        EmailSendingBean.scheduleTask(new SendEmailTask(new EmailDescription(loginBean.getUser().getUsername(),
                sw.toString(), configurationBean.getProperty(Conf.INVOICE_EMAIL_SUBJECT))));
    }

    public void sendTransferAcceptedEmail(Invoice invoice) {
        VelocityContext context = new VelocityContext();

        context.put("name", invoice.getName());

        context.put("invoice_link", getInvoiceLink(invoice));

        String emailTemplate = configurationBean.getProperty(Conf.TICKETS_TRANSFER_RECEIVED_EMAIL);

        StringWriter sw = new StringWriter();
        Velocity.evaluate(context, sw, "emailTransferReceived", emailTemplate);

        EmailSendingBean.scheduleTask(new SendEmailTask(new EmailDescription(loginBean.getUser().getUsername(),
                sw.toString(), configurationBean.getProperty(Conf.TICKETS_TRANSFER_RECEIVED_SUBJECT))));
    }

    @Transactional
    public void sendEmailToAll(String subject, String message) {
        List<String> allEmails = entityManager.createQuery(
                "select distinct(i.user.username) from Invoice i").getResultList();

        allEmails.add(configurationBean.getProperty(Conf.NOTIFY_EMAIL));

        // for each user create new task
        for (String email : allEmails) {
            EmailSendingBean.scheduleTask(new SendEmailTask(new EmailDescription(email, message, subject)));
        }
    }

    private String getInvoiceLink(Invoice invoice) {
        return configurationBean.getProperty(Conf.SYSTEM_URL) + "/pdf/invoice/pdf/" + invoice.getId();
    }

    public void sendForgotEmail(String email, String newPassword) {
        VelocityContext context = new VelocityContext();

        context.put("new_password", newPassword);

        String emailTemplate = configurationBean.getProperty(Conf.PASSWORD_FORGOT_EMAIL);

        StringWriter sw = new StringWriter();
        Velocity.evaluate(context, sw, "emailTransferReceived", emailTemplate);

        EmailSendingBean.scheduleTask(new SendEmailTask(new EmailDescription(email,
                sw.toString(), configurationBean.getProperty(Conf.PASSWORD_FORGOT_SUBJECT))));
    }

    public void sendPaypalError(String message) {
        EmailSendingBean.scheduleTask(new SendEmailTask(
                new EmailDescription(
                        configurationBean.getProperty(Conf.NOTIFY_EMAIL), message, "Paypal processing failed!")));
    }

    public void sendCategoryFinishingEmail(TicketCategory category, long ticketsLeft) {
        VelocityContext context = new VelocityContext();

        context.put("category", category);
        context.put("tickets", ticketsLeft);

        String emailTemplate = configurationBean.getProperty(Conf.TICKETS_FINISHING_EMAIL);
        String subjectTemplate = configurationBean.getProperty(Conf.TICKETS_FINISHING_SUBJECT);

        StringWriter sw = new StringWriter();

        Velocity.evaluate(context, sw, "sendCategoryFinishingEmail", emailTemplate);
        String email = sw.toString();

        sw = new StringWriter();
        Velocity.evaluate(context, sw, "sendCategoryFinishingEmail", subjectTemplate);

        EmailSendingBean.scheduleTask(new SendEmailTask(new EmailDescription(
                configurationBean.getProperty(Conf.NOTIFY_EMAIL), email, sw.toString()
        )));
    }

    public void sendLatePaymentNotification(Invoice invoice) {
        VelocityContext context = new VelocityContext();

        context.put("name", invoice.getName());
        context.put("tickets", invoice.getTickets());

        String emailTemplate = configurationBean.getProperty(Conf.LATE_PAYMENT_EMAIL);

        StringWriter sw = new StringWriter();
        Velocity.evaluate(context, sw, "emailThankYou", emailTemplate);

        EmailSendingBean.scheduleTask(new SendEmailTask(new EmailDescription(invoice.getUser().getUsername(),
                sw.toString(), configurationBean.getProperty(Conf.LATE_PAYMENT_SUBJECT))));
    }
}
