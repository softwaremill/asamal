package pl.softwaremill.asamal.example.service.email;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.helper.AsamalHelper;
import pl.softwaremill.common.sqs.email.SendEmailTask;
import pl.softwaremill.common.sqs.util.EmailDescription;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;

public class EmailService {

    @Inject
    private ConfigurationBean configurationBean;

    @Inject
    private LoginBean loginBean;

    @Inject
    private AsamalHelper asamalHelper;

    @Inject
    private HttpServletRequest request;

    public void sendThankYouEmail(Invoice invoice) {
        VelocityContext context = new VelocityContext();

        context.put("name", invoice.getName());
        context.put("tickets", invoice.getTickets());
        context.put("paymentMethod", invoice.getMethod());

        String emailTemplate = configurationBean.getProperty(Conf.TICKETS_THANKYOU_EMAIL);

        StringWriter sw = new StringWriter();
        Velocity.evaluate(context, sw, "emailThankYou", emailTemplate);

        EmailSendingBean.scheduleTask(new SendEmailTask(new EmailDescription(loginBean.getUser().getUsername(),
                sw.toString(), configurationBean.getProperty(Conf.TICKETS_THANKYOU_EMAIL_SUBJECT), null, null,
                configurationBean.getProperty(Conf.TICKETS_THANKYOU_BCC))));
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

    private String getInvoiceLink(Invoice invoice) {
        String url = "";

        url += (request.isSecure() ? "https://" : "http://");

        url += request.getLocalName();

        url += ((request.isSecure() && request.getLocalPort() != 443) ||
                (!request.isSecure() && request.getLocalPort() != 80)) ? ":" + request.getLocalPort() : "";

        return url + asamalHelper.pdf("invoice", "pdf") +
                "/" + invoice.getId();
    }
}
