package pl.softwaremill.asamal.example.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.service.email.EmailService;
import pl.softwaremill.asamal.example.servlet.paypal.PaypalErrorProcessor;
import pl.softwaremill.asamal.example.servlet.paypal.VerifiedPaymentProcessor;
import com.softwaremill.common.paypal.process.PayPalErrorHandler;
import com.softwaremill.common.paypal.process.processors.PayPalProcessor;
import com.softwaremill.common.paypal.process.processors.PayPalProcessorsFactory;
import com.softwaremill.common.paypal.servlet.IPNServlet;
import pl.softwaremill.common.util.dependency.D;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet to handle paypal notifications
 */
@WebServlet(urlPatterns = "/paypal")
public class AsamalExamplePaypalServlet extends IPNServlet {

    private static final Logger log = LoggerFactory.getLogger(AsamalExamplePaypalServlet.class);

    @Inject
    private ConfigurationBean configurationBean;

    @Inject
    private EmailService emailService;

    @Override
    protected void checkIfInSandbox(ServletConfig config) {
        setPaypalAddress(configurationBean.getBooleanProperty(Conf.PAYPAL_SANDBOX));
    }


    @Override
    protected PayPalErrorHandler getPayPalErrorProcessor() {
        return new PaypalErrorProcessor(emailService);
    }

    @Override
    protected PayPalProcessorsFactory getPayPalProcessorsFactory() {
        return new PayPalProcessorsFactory(VerifiedPaymentProcessor.class){
            @Override
            protected <T extends PayPalProcessor> T createNewInstance(Class<T> processorClass) throws
                    IllegalAccessException, InstantiationException {
                try {
                    return D.inject(processorClass);
                } catch (RuntimeException e) {
                    // otherwise just try the "normal" way, if the bean cannot be injected
                    return super.createNewInstance(processorClass);
                }
            }
        };
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("New PayPal POST");
        super.doPost(request, response);
        log.info("Finished PayPal POST");
    }
}
