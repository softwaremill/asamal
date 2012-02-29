package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.common.cdi.security.Secure;

import javax.inject.Inject;

@Controller("admin")
@Secure("#{login.admin}")
public class Admin extends ControllerBean{

    private TicketCategory ticketCat = new TicketCategory();

    @Inject
    private TicketService ticketService;

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
        doAutoBinding("ticketCat.name", "ticketCat.description", "ticketCat.fromDate", "ticketCat.toDate",
                "ticketCat.numberOfTickets", "ticketCat.price");

        ticketService.addTicketCategory(ticketCat);

        addMessageToFlash("Ticket Category added succesfuly", AsamalContext.MessageSeverity.SUCCESS);
    }

    @Post
    public void editTicketCategory() {
        ticketCat = (TicketCategory) getObjectFromFlash("ticketCat");

        doOptionalAutoBinding("ticketCat.name", "ticketCat.description", "ticketCat.fromDate", "ticketCat.toDate",
                "ticketCat.numberOfTickets", "ticketCat.price");

        ticketService.updateTicketCategory(ticketCat);

        addMessageToFlash("Ticket Category edited succesfuly", AsamalContext.MessageSeverity.SUCCESS);

        redirect("tickets");
    }
    
    @Post
    public void deleteTicketCategory() {
        ticketService.deleteTicketCategory(Long.valueOf(getParameter("id")));
    }
}
