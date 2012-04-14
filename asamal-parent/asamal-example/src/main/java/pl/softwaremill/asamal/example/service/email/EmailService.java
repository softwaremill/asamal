package pl.softwaremill.asamal.example.service.email;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.common.sqs.email.SendEmailTask;
import pl.softwaremill.common.sqs.util.EmailDescription;

import javax.inject.Inject;
import java.io.StringWriter;

public class EmailService {

    @Inject
    private ConfigurationBean configurationBean;

    @Inject
    private LoginBean loginBean;

    public void sendThankYouEmail(Invoice invoice) {
        VelocityContext context = new VelocityContext();

        context.put("name", invoice.getName());
        context.put("tickets", invoice.getTickets());

        String emailTemplate = configurationBean.getProperty(Conf.TICKETS_THANKYOU_EMAIL);

        StringWriter sw = new StringWriter();
        Velocity.evaluate(context, sw, "emailThankYou", emailTemplate);

        EmailSendingBean.scheduleTask(new SendEmailTask(new EmailDescription(loginBean.getUser().getUsername(),
                sw.toString(), configurationBean.getProperty(Conf.TICKETS_THANKYOU_EMAIL_SUBJECT))));
    }
}
