package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.service.conf.ConfigurationService;
import pl.softwaremill.asamal.example.service.exception.TicketsExceededException;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.common.cdi.security.Secure;

import javax.inject.Inject;

@Controller("admin")
@Secure("#{login.admin}")
public class Admin extends ControllerBean{

    private TicketCategory ticketCat = new TicketCategory();

    @Inject
    private TicketService ticketService;
    
    private final static String[] TICKET_CATEGORY_PARAMS = new String[]{"ticketCat.name", "ticketCat.description",
            "ticketCat.fromDate", "ticketCat.toDate", "ticketCat.numberOfTickets", "ticketCat.price",
            "ticketCat.invoiceDescription"};

    public TicketCategory getTicketCat() {
        return ticketCat;
    }

    public void setTicketCat(TicketCategory ticketCat) {
        this.ticketCat = ticketCat;
    }

    @Get
    public void tickets() {
        putInContext("ticketCat", ticketCat);
    }

    @Get
    public void editTicketCat() {
        ticketCat = ticketService.loadCategory(Long.valueOf(getExtraPath()[0]));

        putInContext("ticketCat", ticketCat);

        addObjectToFlash("ticketCat", ticketCat);
    }

    @Post
    public void addTicketCategory() {
        doAutoBinding(TICKET_CATEGORY_PARAMS);

        try {
            ticketService.addTicketCategory(ticketCat);

            addMessageToFlash("Ticket Category added succesfuly", AsamalContext.MessageSeverity.SUCCESS);
        } catch (TicketsExceededException e) {
            addMessageToFlash(e.getMessage(), AsamalContext.MessageSeverity.ERR);
        }
    }

    @Post
    public void editTicketCategory() {
        ticketCat = (TicketCategory) getObjectFromFlash("ticketCat");

        doOptionalAutoBinding(TICKET_CATEGORY_PARAMS);

        ticketService.updateTicketCategory(ticketCat);

        addMessageToFlash("Ticket Category edited succesfuly", AsamalContext.MessageSeverity.SUCCESS);

        redirect("tickets");
    }
    
    @Post
    public void deleteTicketCategory() {
        ticketService.deleteTicketCategory(Long.valueOf(getParameter("id")));
    }

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private ConfigurationBean configBean;

    @Get
    public void configuration() {

    }

    @Post
    public void saveConfig() {
        for (Conf conf : Conf.values()) {
            configurationService.saveProperty(conf, getParameter(conf.toString()));
        }

        addMessageToFlash(getFromMessageBundle("configuration.saved"), AsamalContext.MessageSeverity.SUCCESS);

        // reset config cache
        configBean.setProperties(null);

        redirect("configuration");
    }
}
