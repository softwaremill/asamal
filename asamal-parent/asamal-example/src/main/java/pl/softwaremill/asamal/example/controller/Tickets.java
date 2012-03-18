package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Filters;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.example.filters.AuthorizationFilter;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.model.ticket.Ticket;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.service.ticket.TicketService;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tickets controller
 *
 * User: szimano
 */
@Controller("tickets")
@Filters(AuthorizationFilter.class)
public class Tickets extends ControllerBean implements Serializable {
    
    private final static Integer maxTickets = 5;
    private static final String NUMBER_OF_TICKETS_PREFIX = "numberOfTickets-";

    @Inject
    private TicketService ticketService;
    
    private Invoice invoice = new Invoice();
    
    private List<TicketCategory> availableCategories;

    private Ticket[][] ticketsByCategory;

    @Inject
    private LoginBean loginBean;
    
    @Get
    public void buy() {
    }

    @Post
    public void changeNumber() {
        bindTickets();

        Map<String, Integer> toBuy = new HashMap<String, Integer>();

        for (String paramName : getParameterNames()) {
            if (paramName.startsWith(NUMBER_OF_TICKETS_PREFIX)) {
                toBuy.put(paramName.substring(NUMBER_OF_TICKETS_PREFIX.length()),
                        new Integer(getParameter(paramName)));
            }
        }
        putInContext("ticketsToBuy", toBuy);
    }

    @Post
    public void doBuy() {
        bindTickets();

        boolean allGood = validateBean("invoice", invoice);

        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setUser(loginBean.getUser());

        for (int i = 0; i < ticketsByCategory.length; i++) {
            for (int j = 0; j < ticketsByCategory[i].length; j++) {
                if (!validateBean("attendeesByCategory["+i+"]["+j+"]", ticketsByCategory[i][j])) {
                    allGood = false;
                }
            }
        }

        if (!allGood) {
            addMessageToFlash(getFromMessageBundle("tickets.validation.errors"), AsamalContext.MessageSeverity.ERR);

            includeView("buy");
        }
        else {
            ticketService.addInvoice(invoice);

            addMessageToFlash(getFromMessageBundle("tickets.buy.ok"), AsamalContext.MessageSeverity.SUCCESS);
            redirect("home", "index");
        }
    }

    private void bindTickets() {
        // initiate first

        ticketsByCategory = new Ticket[getAvailableCategories().size()][];
        ArrayList<String> paramNames = new ArrayList<String>();

        for (int i = 0; i < getAvailableCategories().size(); i++) {
            int numberOfAttendees = Integer.parseInt(
                    getParameter(NUMBER_OF_TICKETS_PREFIX + getAvailableCategories().get(i).getIdName())
            );
            ticketsByCategory[i] = new Ticket[numberOfAttendees];
            for (int j = 0; j < numberOfAttendees; j++) {
                ticketsByCategory[i][j] = new Ticket();
                
                String attendeePrefix = "ticketsByCategory["+i+"]["+j+"]";
                paramNames.add(attendeePrefix+".firstName");
                paramNames.add(attendeePrefix+".lastName");
            }
        }

        // bind attendees
        doOptionalAutoBinding(paramNames.toArray(new String[paramNames.size()]));

        // bind invoice
        doOptionalAutoBinding("invoice.name", "invoice.companyName", "invoice.vat",
                "invoice.address", "invoice.postalCode", "invoice.city", "invoice.country");
    }

    public List<TicketCategory> getAvailableCategories() {
        if (availableCategories == null) {
            availableCategories = ticketService.getAvailableCategories();
        }

        return availableCategories;
    }
    
    public Integer getMaxTickets(TicketCategory category) {
        Integer soldTickets = ticketService.getSoldTicketsInCategory(category);
        Integer ticketsLeft = category.getNumberOfTickets() - soldTickets;

        return (ticketsLeft > maxTickets) ? maxTickets : ticketsLeft;
    }

    public Ticket[][] getTicketsByCategory() {
        return ticketsByCategory;
    }

    public void setTicketsByCategory(Ticket[][] ticketsByCategory) {
        this.ticketsByCategory = ticketsByCategory;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
