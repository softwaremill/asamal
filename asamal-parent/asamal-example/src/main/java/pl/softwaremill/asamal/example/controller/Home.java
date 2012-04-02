package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Filters;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.example.filters.AuthorizationFilter;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.service.ticket.TicketService;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("home")
@Filters(AuthorizationFilter.class)
public class Home extends ControllerBean implements Serializable {

    @Inject
    private TicketService ticketService;

    @Inject
    private LoginBean login;

    @Inject
    private ConfigurationBean configurationBean;

    @Get
    public void index() {
        putInContext("tickets", ticketService.getTicketsForUser(login.getUser()));
    }
    
    public String getInvoiceId(Invoice invoice) {
        return configurationBean.getProperty(Conf.INVOICE_ID).toLowerCase().replaceAll("/", "-") + invoice.getId();
    }

}
