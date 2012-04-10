package pl.softwaremill.asamal.example.servlet;

import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.servlet.paypal.PaypalErrorProcessor;
import pl.softwaremill.asamal.example.servlet.paypal.VerifiedPaymentProcessor;
import pl.softwaremill.common.paypal.process.PayPalErrorHandler;
import pl.softwaremill.common.paypal.process.processors.PayPalProcessor;
import pl.softwaremill.common.paypal.process.processors.PayPalProcessorsFactory;
import pl.softwaremill.common.paypal.servlet.IPNServlet;
import pl.softwaremill.common.util.dependency.D;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;

/**
 * Servlet to handle paypal notifications
 */
@WebServlet(urlPatterns = "/paypal")
public class AsamalExamplePaypalServlet extends IPNServlet {

    @Inject
    private ConfigurationBean configurationBean;

    @Override
    protected void checkIfInSandbox(ServletConfig config) {
        setPaypalAddress(configurationBean.getBooleanProperty(Conf.PAYPAL_SANDBOX));
    }


    @Override
    protected PayPalErrorHandler getPayPalErrorProcessor() {
        return new PaypalErrorProcessor();
    }

    @Override
    protected PayPalProcessorsFactory getPayPalProcessorsFactory() {
        return new PayPalProcessorsFactory(VerifiedPaymentProcessor.class){
            @Override
            protected <T extends PayPalProcessor> T createNewInstance(Class<T> processorClass) throws
                    IllegalAccessException, InstantiationException {
                return D.inject(processorClass);
            }
        };
    }
}
